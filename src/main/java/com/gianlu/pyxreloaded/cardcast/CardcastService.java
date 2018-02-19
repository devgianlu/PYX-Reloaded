package com.gianlu.pyxreloaded.cardcast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;


public class CardcastService {
    private final static AtomicInteger cardId = new AtomicInteger(Integer.MIN_VALUE);
    private static final Logger LOG = Logger.getLogger(CardcastService.class);
    private static final String HOSTNAME = "api.cardcastgame.com";

    public static int getNewCardId() {
        synchronized (cardId) {
            return cardId.decrementAndGet();
        }
    }

    /**
     * Base URL to the Cardcast API.
     */
    private static final String BASE_URL = "https://" + HOSTNAME + "/v1/decks/";

    /**
     * URL to the Cardcast API for information about a card set. The only format replacement is the
     * string deck ID.
     */
    private static final String CARD_SET_INFO_URL_FORMAT_STRING = BASE_URL + "%s";

    /**
     * URL to the Cardcast API for cards in a card set. The only format replacement is the string
     * deck ID.
     */
    private static final String CARD_SET_CARDS_URL_FORMAT_STRING = CARD_SET_INFO_URL_FORMAT_STRING + "/cards";

    /**
     * Connection timeout
     */
    private static final int TIMEOUT = (int) TimeUnit.SECONDS.toMillis(3);

    /**
     * How long to cache nonexistent card sets, or after an error occurs while querying for the card
     * set. We need to do this to prevent DoS attacks.
     */
    private static final long INVALID_SET_CACHE_LIFETIME = TimeUnit.SECONDS.toMillis(30);

    /**
     * How long to cache valid card sets.
     */
    private static final long VALID_SET_CACHE_LIFETIME = TimeUnit.MINUTES.toMillis(15);
    private static final Pattern validIdPattern = Pattern.compile("[A-Z0-9]{5}");
    private static final Map<String, SoftReference<CardcastCacheEntry>> cache = Collections.synchronizedMap(new HashMap<String, SoftReference<CardcastCacheEntry>>());
    private static final HttpClient client = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setConnectTimeout(TIMEOUT)
                    .setSocketTimeout(TIMEOUT)
                    .setConnectionRequestTimeout(TIMEOUT)
                    .build())
            .build();

    @Nullable
    private CardcastCacheEntry checkCache(String setId) {
        SoftReference<CardcastCacheEntry> soft = cache.get(setId);
        if (soft == null) return null;
        else return soft.get();
    }

    public CardcastDeck loadSet(String setId) {
        if (!validIdPattern.matcher(setId).matches()) return null;

        CardcastCacheEntry cached = checkCache(setId);
        if (cached != null && cached.expires > System.currentTimeMillis()) {
            LOG.info(String.format("Using cache: %s=%s", setId, cached.deck));
            return cached.deck;
        } else if (cached != null) {
            LOG.info(String.format("Cache stale: %s", setId));
        } else {
            LOG.info(String.format("Cache miss: %s", setId));
        }

        JsonParser parser = new JsonParser();

        try {
            String infoContent = getUrlContent(String.format(CARD_SET_INFO_URL_FORMAT_STRING, setId));
            if (infoContent == null) {
                // failed to load
                cacheMissingSet(setId);
                return null;
            }

            JsonObject info = parser.parse(infoContent).getAsJsonObject();

            String cardContent = getUrlContent(String.format(CARD_SET_CARDS_URL_FORMAT_STRING, setId));
            if (cardContent == null) {
                // failed to load
                cacheMissingSet(setId);
                return null;
            }

            JsonObject cards = parser.parse(cardContent).getAsJsonObject();

            String name = info.get("name").getAsString();
            String description = info.get("description").getAsString();
            if (name == null || description == null || name.isEmpty()) {
                // We require a name. Blank description is acceptable, but cannot be null.
                cacheMissingSet(setId);
                return null;
            }

            CardcastDeck deck = new CardcastDeck(StringEscapeUtils.escapeXml11(name), setId, StringEscapeUtils.escapeXml11(description));

            JsonArray blacks = cards.getAsJsonArray("calls");
            if (blacks != null) {
                for (JsonElement black : blacks) {
                    JsonArray texts = black.getAsJsonObject().getAsJsonArray("text");
                    if (texts != null) {
                        List<String> strs = new ArrayList<>(texts.size());
                        for (JsonElement text : texts) strs.add(text.getAsString());
                        String text = StringUtils.join(strs, "____");
                        int pick = strs.size() - 1;
                        int draw = (pick >= 3 ? pick - 1 : 0);
                        deck.getBlackCards().add(new CardcastBlackCard(getNewCardId(), StringEscapeUtils.escapeXml11(text), draw, pick, setId));
                    }
                }
            }

            JsonArray whites = cards.getAsJsonArray("responses");
            if (whites != null) {
                for (JsonElement white : whites) {
                    JsonArray texts = white.getAsJsonObject().getAsJsonArray("text");
                    if (texts != null) {
                        // The white cards should only ever have one element in text, but let's be safe.
                        List<String> strs = new ArrayList<>(texts.size());
                        for (JsonElement text : texts) {
                            String cardCastString = text.getAsString();
                            if (cardCastString.isEmpty()) {
                                // skip blank segments
                                continue;
                            }

                            StringBuilder pyxString = new StringBuilder();

                            // Cardcast's recommended format is to not capitalize the first letter
                            pyxString.append(cardCastString.substring(0, 1).toUpperCase());
                            pyxString.append(cardCastString.substring(1));

                            // Cardcast's recommended format is to not include a period
                            if (Character.isLetterOrDigit(cardCastString.charAt(cardCastString.length() - 1)))
                                pyxString.append('.');

                            // Cardcast's white cards are now formatted consistently with pyx cards
                            strs.add(pyxString.toString());
                        }

                        String text = StringUtils.join(strs, "");
                        // don't add blank cards, they don't do anything
                        if (!text.isEmpty())
                            deck.getWhiteCards().add(new CardcastWhiteCard(getNewCardId(), StringEscapeUtils.escapeXml11(text), setId));
                    }
                }
            }

            cacheSet(setId, deck);
            return deck;
        } catch (Exception ex) {
            LOG.error(String.format("Unable to load deck %s from Cardcast", setId), ex);
            cacheMissingSet(setId);
            return null;
        }
    }

    private void cachePut(String setId, CardcastDeck deck, long timeout) {
        LOG.info(String.format("Caching %s=%s for %d ms", setId, deck, timeout));
        cache.put(setId, new SoftReference<>(new CardcastCacheEntry(timeout, deck)));
    }

    private void cacheSet(String setId, CardcastDeck deck) {
        cachePut(setId, deck, VALID_SET_CACHE_LIFETIME);
    }

    private void cacheMissingSet(String setId) {
        cachePut(setId, null, INVALID_SET_CACHE_LIFETIME);
    }

    @Nullable
    private String getUrlContent(String urlStr) throws IOException {
        HttpResponse resp = client.execute(new HttpGet(urlStr));

        StatusLine sl = resp.getStatusLine();
        if (sl.getStatusCode() != HttpStatus.SC_OK) {
            LOG.error(String.format("Got HTTP response code %s from Cardcast for %s", sl, urlStr));
            return null;
        }

        HttpEntity entity = resp.getEntity();
        String contentType = entity.getContentType().getValue();
        if (!Objects.equals(contentType, "application/json")) {
            LOG.error(String.format("Got content-type %s from Cardcast for %s", contentType, urlStr));
            return null;
        }

        return EntityUtils.toString(entity);
    }

    private class CardcastCacheEntry {
        final long expires;
        final CardcastDeck deck;

        CardcastCacheEntry(long cacheLifetime, CardcastDeck deck) {
            this.expires = System.currentTimeMillis() + cacheLifetime;
            this.deck = deck;
        }
    }
}
