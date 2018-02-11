package com.gianlu.pyxreloaded;

import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import org.jetbrains.annotations.NotNull;

public class CacheControlHandler implements HttpHandler {
    private final ResourceHandler handler;
    private final String cacheControl;
    private final String pragma;
    private final long expires;

    private CacheControlHandler(ResourceHandler handler, String cacheControl, String pragma, long expires) {
        this.handler = handler;
        this.cacheControl = cacheControl;
        this.pragma = pragma;
        this.expires = expires;
    }

    @NotNull
    public static CacheControlHandler disableCache(ResourceHandler handler) {
        handler.setCachable(Predicates.falsePredicate());
        handler.setCacheTime(0);
        return new CacheControlHandler(handler, "no-cache, no-store, must-revalidate", "no-cache", 0);
    }

    @NotNull
    public static CacheControlHandler browserManagedCache(ResourceHandler handler) {
        return new CacheControlHandler(handler, null, null, -1);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        handler.handleRequest(exchange);

        HeaderMap headers = exchange.getResponseHeaders();
        if (cacheControl == null) headers.remove(Headers.CACHE_CONTROL);
        else headers.add(Headers.CACHE_CONTROL, cacheControl);
        if (pragma == null) headers.remove(Headers.PRAGMA);
        else headers.add(Headers.PRAGMA, pragma);
        if (expires == -1) headers.remove(Headers.EXPIRES);
        else headers.add(Headers.EXPIRES, expires);
    }
}
