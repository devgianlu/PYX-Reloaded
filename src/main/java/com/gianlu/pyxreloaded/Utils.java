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

    public static boolean isVersionNewer(String older, String newer) {
        String[] olderSplit = older.split("\\.");
        String[] newerSplit = newer.split("\\.");

        for (int i = 0; i < olderSplit.length && i < newerSplit.length; i++) {
            int olderNum = Integer.parseInt(olderSplit[i]);
            int newerNum = Integer.parseInt(newerSplit[i]);
            if (newerNum > olderNum) return true;
        }

        return false;
    }

    public static JsonArray toIntsJsonArray(Collection<Integer> items) {
        JsonArray jsonArray = new JsonArray(items.size());
        for (int item : items) jsonArray.add(item);
        return jsonArray;
    }

    @NotNull
    public static String getServerVersion(Package pkg) {
        String version = pkg.getImplementationVersion();
        if (version == null) version = pkg.getSpecificationVersion();
        if (version == null) version = System.getenv("version");
        if (version == null) version = "debug";
        return version;
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

    public static boolean contains(String[] array, String val) {
        for (String str : array)
            if (str.equals(val)) return true;
        return false;
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
