package com.gianlu.pyxreloaded.paths;

import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.jetbrains.annotations.NotNull;

public class VersionPath implements HttpHandler {
    private final String json;

    public VersionPath() {
        JsonObject obj = new JsonObject();
        Package pkg = Package.getPackage("com.gianlu.pyxreloaded");
        obj.addProperty("version", getServerVersion(pkg));
        json = obj.toString();
    }

    @NotNull
    private static String getServerVersion(Package pkg) {
        String version = pkg.getImplementationVersion();
        if (version == null) version = pkg.getSpecificationVersion();
        if (version == null) version = System.getenv("version");
        if (version == null) version = "debug";
        return version;
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
