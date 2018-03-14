package com.gianlu.pyxreloaded.paths;

import com.gianlu.pyxreloaded.singletons.Preferences;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.Headers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.logging.Logger;

public class WebManifestPath implements HttpHandler {
    private static final Logger logger = Logger.getLogger(WebManifestPath.class.getSimpleName());
    private final JsonObject baseManifest;
    private final String baseManifestString;

    public WebManifestPath(Preferences preferences) throws FileNotFoundException {
        File manifest = new File(preferences.getString("webContent", "./WebContent"), "manifest.json");
        if (!manifest.exists() && !manifest.canRead()) {
            logger.severe("Missing base manifest file. Loading empty.");
            baseManifest = new JsonObject();
        } else {
            baseManifest = new JsonParser().parse(new InputStreamReader(new FileInputStream(manifest))).getAsJsonObject();
        }

        baseManifestString = baseManifest.toString();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.startBlocking();
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json");

        Cookie primaryColor = exchange.getRequestCookies().get("PYX-Theme-Primary");
        if (primaryColor == null) {
            exchange.getResponseSender().send(baseManifestString);
        } else {
            JsonObject manifest = baseManifest.deepCopy();
            manifest.addProperty("theme_color", URLDecoder.decode(primaryColor.getValue(), "UTF-8"));
            exchange.getResponseSender().send(manifest.toString());
        }
    }
}
