package com.gianlu.pyxreloaded.paths;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.twitter.TwitterAuthHelper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.concurrent.TimeUnit;

public class TwitterCallbackPath implements HttpHandler {
    private static final int COOKIE_MAX_AGE = (int) TimeUnit.MINUTES.toSeconds(5); // sec
    private static final String REDIRECT_LOCATION = "/?" + Consts.GeneralKeys.AUTH_TYPE + "=" + Consts.AuthType.TWITTER;
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

                String accessToken = helper.accessToken(token, verifier);
                CookieImpl cookie = new CookieImpl("PYX-Twitter-Token", accessToken);
                cookie.setMaxAge(COOKIE_MAX_AGE);
                exchange.setResponseCookie(cookie);
                exchange.getResponseHeaders().add(Headers.LOCATION, REDIRECT_LOCATION);
                exchange.setStatusCode(StatusCodes.TEMPORARY_REDIRECT);
            } catch (Throwable ex) {
                ex.printStackTrace();
                throw ex;
            }
        } else {
            exchange.setStatusCode(StatusCodes.METHOD_NOT_ALLOWED);
        }
    }
}
