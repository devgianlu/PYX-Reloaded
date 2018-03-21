package com.gianlu.pyxreloaded.paths;

import com.gianlu.pyxreloaded.Utils;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class VersionPath implements HttpHandler {
    private final String json;

    public VersionPath() {
        JsonObject obj = new JsonObject();
        Package pkg = Package.getPackage("com.gianlu.pyxreloaded");
        obj.addProperty("version", Utils.getServerVersion(pkg));
        json = obj.toString();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        exchange.startBlocking();
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(json);
    }
}
