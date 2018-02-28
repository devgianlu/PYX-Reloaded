package com.gianlu.pyxreloaded.paths;

import com.gianlu.pyxreloaded.twitter.TwitterAuthHelper;
import com.gianlu.pyxreloaded.twitter.TwitterProfileInfo;
import com.github.scribejava.core.model.OAuth1AccessToken;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;

public class TwitterCallbackPath implements HttpHandler {
    private final TwitterAuthHelper helper;

    public TwitterCallbackPath(TwitterAuthHelper helper) {
        this.helper = helper;
    }

    @Nullable
    private static String extractToken(HttpServerExchange exchange) {
        Deque<String> deque = exchange.getQueryParameters().get("oauth_token");
        return deque.isEmpty() ? null : deque.getFirst();
    }

    @Nullable
    private static String extractVerifier(HttpServerExchange exchange) {
        Deque<String> deque = exchange.getQueryParameters().get("oauth_verifier");
        return deque.isEmpty() ? null : deque.getFirst();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.getRequestMethod() == Methods.GET) {
            exchange.startBlocking();
            if (exchange.isInIoThread()) {
                exchange.dispatch(this);
                return;
            }

            try {
                String token = extractToken(exchange);
                String verifier = extractVerifier(exchange);

                if (token == null || verifier == null)
                    throw new Exception("Invalid request!");

                OAuth1AccessToken accessToken = helper.accessToken(token, verifier);
                TwitterProfileInfo info = helper.info(accessToken);
                System.out.println(info);
            } catch (Throwable ex) {
                ex.printStackTrace();
                throw ex;
            }
        } else {
            exchange.setStatusCode(StatusCodes.METHOD_NOT_ALLOWED);
        }
    }
}
