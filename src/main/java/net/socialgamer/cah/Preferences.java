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

    private Preferences() {
    }

    public static Preferences load() throws IOException {
        File file = new File("preferences.json");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")))) {
                StringBuilder builder = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) builder.append(line);

                return new Preferences(builder.toString());
            }
        } else {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            return new Preferences();
        }
    }

    public MinDefaultMax getMinDefaultMax(String key, int min, int def, int max) {
        JsonElement obj = get(key);
        if (obj == null) return new MinDefaultMax(min, def, max);
        else return new MinDefaultMax(obj.getAsJsonObject());
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

    public static class MinDefaultMax {
        public final int min;
        public final int def;
        public final int max;

        private MinDefaultMax(int min, int def, int max) {
            this.min = min;
            this.def = def;
            this.max = max;
        }

        private MinDefaultMax(JsonObject obj) {
            min = obj.get("min").getAsInt();
            def = obj.get("default").getAsInt();
            max = obj.get("max").getAsInt();
        }
    }
}
