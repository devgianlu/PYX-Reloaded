package com.gianlu.pyxreloaded.paths;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.Utils;
import com.gianlu.pyxreloaded.socials.github.GithubAuthHelper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class GithubCallbackPath implements HttpHandler {
    private static final Logger logger = Logger.getLogger(GithubCallbackPath.class.getSimpleName());
    private static final int COOKIE_MAX_AGE = (int) TimeUnit.MINUTES.toSeconds(5); // sec
    private static final String REDIRECT_LOCATION = "/?" + Consts.GeneralKeys.AUTH_TYPE + "=" + Consts.AuthType.GITHUB;
    private final GithubAuthHelper githubHelper;

    public GithubCallbackPath(@NotNull GithubAuthHelper githubHelper) {
        this.githubHelper = githubHelper;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.startBlocking();
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        String code = Utils.extractParam(exchange, "code");
        if (code == null) {
            exchange.setStatusCode(StatusCodes.BAD_REQUEST);
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
            logger.error("Failed processing the request: " + exchange, ex);
            throw ex;
        }
    }
}
