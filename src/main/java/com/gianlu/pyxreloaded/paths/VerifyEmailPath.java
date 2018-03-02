package com.gianlu.pyxreloaded.paths;

import com.gianlu.pyxreloaded.singletons.Emails;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;

public class VerifyEmailPath implements HttpHandler {
    private final Emails emails;

    public VerifyEmailPath(Emails emails) {
        this.emails = emails;
    }

    @Nullable
    private String extractToken(HttpServerExchange exchange) {
        Deque<String> deque = exchange.getQueryParameters().get("token");
        return deque.isEmpty() ? null : deque.getFirst();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        exchange.startBlocking();
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        String token = extractToken(exchange);
        if (token == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
            return;
        }

        if (emails.tryVerify(token)) {
            exchange.getResponseHeaders().add(Headers.LOCATION, "/");
            exchange.getResponseSender().send("Nice!");
            exchange.setStatusCode(StatusCodes.OK);
        } else {
            exchange.getResponseSender().send("Something went wrong!");
            exchange.setStatusCode(StatusCodes.FORBIDDEN);
        }
    }
}
