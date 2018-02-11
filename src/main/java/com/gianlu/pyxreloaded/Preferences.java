package com.gianlu.pyxreloaded;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.logging.Logger;

public class Preferences extends HashMap<String, JsonElement> {
    private static final Logger logger = Logger.getLogger(Preferences.class.getSimpleName());

    private Preferences(String json) {
        JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
        for (String key : obj.keySet()) put(key, obj.get(key));
    }

    private Preferences() {
    }

    private static Preferences loadDefault() {
        logger.info("Preferences not found. Loaded default.");
        return new Preferences();
    }

    public static Preferences load(String[] args) throws IOException {
        String prefs = "preferences.json.default";
        if (args.length >= 2) {
            for (int i = 0; i < args.length / 2; i++) {
                if (args[i * 2].equals("--prefs")) prefs = args[i * 2 + 1];
            }
        }

        File file = new File(prefs);
        if (file.exists() && file.canRead()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")))) {
                StringBuilder builder = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) builder.append(line);

                String json = builder.toString();
                if (json.isEmpty()) return loadDefault();

                Preferences preferences = new Preferences(json);
                logger.info("Loaded preferences: " + preferences);
                return preferences;
            }
        } else {
            return loadDefault();
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

    public boolean getBoolean(String key, boolean fallback) {
        JsonElement obj = get(key);
        if (obj == null) return fallback;
        else return obj.getAsBoolean();
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

        public JsonWrapper toJson() {
            JsonWrapper obj = new JsonWrapper();
            obj.add(Consts.MinMaxData.MIN, min);
            obj.add(Consts.MinMaxData.DEFAULT, def);
            obj.add(Consts.MinMaxData.MAX, max);
            return obj;
        }
    }
}
