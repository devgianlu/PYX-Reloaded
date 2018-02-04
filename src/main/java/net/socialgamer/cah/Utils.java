package net.socialgamer.cah;

import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.Random;
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

    public static String generateAlphanumericString(int length) {
        StringBuilder builder = new StringBuilder();
        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            if (random.nextBoolean()) builder.append(String.valueOf(random.nextInt(10)));
            else builder.append(ALPHABET.charAt(random.nextInt(26)));
        }

        return builder.toString();
    }
}
