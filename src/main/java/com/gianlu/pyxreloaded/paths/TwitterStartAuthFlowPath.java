package com.gianlu.pyxreloaded.paths;

import com.gianlu.pyxreloaded.socials.twitter.TwitterAuthHelper;
import com.github.scribejava.core.model.OAuth1RequestToken;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TwitterStartAuthFlowPath implements HttpHandler {
    private static final Logger logger = Logger.getLogger(TwitterStartAuthFlowPath.class.getSimpleName());
    private static final int COOKIE_MAX_AGE = (int) TimeUnit.MINUTES.toSeconds(5); // sec
    private final TwitterAuthHelper helper;

    public TwitterStartAuthFlowPath(TwitterAuthHelper helper) {
        this.helper = helper;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.startBlocking();
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        try {
            OAuth1RequestToken token = helper.requestToken();
            CookieImpl cookie = new CookieImpl("PYX-Twitter-Token", token.getRawResponse());
            cookie.setMaxAge(COOKIE_MAX_AGE);
            exchange.setResponseCookie(cookie);
            exchange.getResponseHeaders().add(Headers.LOCATION, helper.authorizationUrl(token) + "&force_login=false");
            exchange.setStatusCode(StatusCodes.TEMPORARY_REDIRECT);
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Failed processing the request: " + exchange, ex);
            throw ex;
        }
    }
}
