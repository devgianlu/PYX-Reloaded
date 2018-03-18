package com.gianlu.pyxreloaded;

import com.gianlu.pyxreloaded.cards.WhiteCard;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import org.apache.http.NameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static JsonArray toIntsJsonArray(Collection<Integer> items) {
        JsonArray jsonArray = new JsonArray(items.size());
        for (int item : items) jsonArray.add(item);
        return jsonArray;
    }

    public static JsonArray toStringsJsonArray(Collection<String> items) {
        JsonArray jsonArray = new JsonArray(items.size());
        for (String item : items) jsonArray.add(item);
        return jsonArray;
    }

    public static Map<String, String> toMap(JsonObject obj) {
        HashMap<String, String> map = new HashMap<>();
        for (String key : obj.keySet()) map.put(key, obj.get(key).getAsString());
        return map;
    }

    @Nullable
    public static String extractParam(HttpServerExchange exchange, String key) {
        Deque<String> deque = exchange.getQueryParameters().get(key);
        return deque == null || deque.isEmpty() ? null : deque.getFirst();
    }

    @NotNull
    public static String joinCardIds(Collection<WhiteCard> items, String separator) {
        if (items == null) return "";

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (WhiteCard item : items) {
            if (!first) builder.append(separator);
            builder.append(item.getId());
            first = false;
        }

        return builder.toString();
    }

    @NotNull
    public static String generateAlphanumericString(int length) {
        StringBuilder builder = new StringBuilder();
        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            if (random.nextBoolean()) builder.append(String.valueOf(random.nextInt(10)));
            else builder.append(ALPHABET.charAt(random.nextInt(26)));
        }

        return builder.toString();
    }

    @Nullable
    public static String get(List<NameValuePair> pairs, String name) {
        for (NameValuePair pair : pairs)
            if (Objects.equals(pair.getName(), name))
                return pair.getValue();

        return null;
    }
}
