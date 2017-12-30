package net.socialgamer.cah;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Set;

public class Utils {

    public static JsonArray toJsonArray(int[] array) {
        JsonArray jsonArray = new JsonArray(array.length);
        for (int item : array) jsonArray.add(item);
        return jsonArray;
    }

    public static JsonArray toJsonArray(Set<Integer> set) {
        JsonArray jsonArray = new JsonArray(set.size());
        for (int item : set) jsonArray.add(item);
        return jsonArray;
    }

    public static JsonObject singletonJsonObject(String key, JsonElement element) {
        JsonObject json = new JsonObject();
        json.add(key, element);
        return json;
    }

    public static JsonArray singletonJsonArray(JsonElement element) {
        JsonArray json = new JsonArray(1);
        json.add(element);
        return json;
    }
}
