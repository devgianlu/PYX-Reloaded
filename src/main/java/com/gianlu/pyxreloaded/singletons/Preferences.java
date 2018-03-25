package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.Charset;

public final class Preferences {
    private static final Logger logger = Logger.getLogger(Preferences.class.getSimpleName());
    private final JsonObject root;

    private Preferences(String json) {
        root = new JsonParser().parse(json).getAsJsonObject();
    }

    private Preferences() {
        root = new JsonObject();
    }

    @NotNull
    private static Preferences loadDefault() {
        logger.info("Preferences not found. Loaded default.");
        return new Preferences();
    }

    public static Preferences load(String[] args) throws IOException {
        String prefs = "preferences.json";
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

    @Nullable
    private JsonElement get(String key) {
        JsonObject parent = root;
        String lastPath = key;
        if (key.contains("/")) {
            String[] pathsTemp = key.split("/");
            lastPath = pathsTemp[pathsTemp.length - 1];
            String[] paths = new String[pathsTemp.length - 1];
            System.arraycopy(pathsTemp, 0, paths, 0, paths.length);

            for (String path : paths)
                parent = parent.getAsJsonObject(path);
        }

        return parent == null ? null : parent.get(lastPath);
    }

    @NotNull
    public MinDefaultMax getMinDefaultMax(String key, int min, int def, int max) {
        JsonElement obj = get(key);
        if (obj == null) return new MinDefaultMax(min, def, max);
        else return new MinDefaultMax(obj.getAsJsonObject());
    }

    public String getString(String key, @Nullable String fallback) {
        JsonElement obj = get(key);
        if (obj == null) return fallback;
        else return obj.getAsString();
    }

    public String getStringNotEmpty(String key, @Nullable String fallback) {
        String str = getString(key, fallback);
        if (str != null && str.isEmpty()) return fallback;
        return str;
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

    @Override
    public String toString() {
        return root.toString();
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
