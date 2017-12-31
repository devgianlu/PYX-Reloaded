package net.socialgamer.cah;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;

public class Preferences extends HashMap<String, JsonElement> {

    private Preferences(String json) {
        JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
        for (String key : obj.keySet()) put(key, obj.get(key));
    }

    public static Preferences load() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("preferences.json")), Charset.forName("UTF-8")))) {
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) builder.append(line);

            return new Preferences(builder.toString());
        }
    }

    public String getString(String key, String fallback) {
        JsonElement obj = get(key);
        if (obj == null) return fallback;
        else return obj.getAsString();
    }

    public int getInt(String key, int fallback) {
        JsonElement obj = get(key);
        if (obj == null) return fallback;
        else return obj.getAsInt();
    }
}
