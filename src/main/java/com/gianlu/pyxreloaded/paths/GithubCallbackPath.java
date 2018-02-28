package com.gianlu.pyxreloaded.paths;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.github.GithubAuthHelper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GithubCallbackPath implements HttpHandler {
    private static final Logger logger = Logger.getLogger(GithubCallbackPath.class.getSimpleName());
    private static final int COOKIE_MAX_AGE = (int) TimeUnit.MINUTES.toSeconds(5); // sec
    private static final String REDIRECT_LOCATION = "/?" + Consts.GeneralKeys.AUTH_TYPE + "=" + Consts.AuthType.GITHUB;
    private final GithubAuthHelper githubHelper;

    public GithubCallbackPath(GithubAuthHelper githubHelper) {
        this.githubHelper = githubHelper;
    }

    @Nullable
    private static String extractCode(HttpServerExchange exchange) {
        Deque<String> deque = exchange.getQueryParameters().get("code");
        return deque.isEmpty() ? null : deque.getFirst();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.getRequestMethod() == Methods.GET) {
            String code = extractCode(exchange);
            if (code == null) {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                exchange.endExchange();
                return;
            }

            exchange.startBlocking();
            if (exchange.isInIoThread()) {
                exchange.dispatch(this);
                return;
            }

            try {
                String accessToken = githubHelper.exchangeCode(code);

                CookieImpl cookie = new CookieImpl("PYX-Github-Token", accessToken);
                cookie.setMaxAge(COOKIE_MAX_AGE);
                exchange.setResponseCookie(cookie);
                exchange.getResponseHeaders().add(Headers.LOCATION, REDIRECT_LOCATION);
                exchange.setStatusCode(StatusCodes.TEMPORARY_REDIRECT);
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, "Failed processing the request: " + exchange, ex);
                throw ex;
            }
        } else {
            exchange.setStatusCode(StatusCodes.METHOD_NOT_ALLOWED);
        }
    }
}
