package com.gianlu.pyxreloaded.paths;

import com.gianlu.pyxreloaded.twitter.TwitterAuthHelper;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

public class TwitterStartAuthFlowPath implements HttpHandler {
    private final TwitterAuthHelper helper;

    public TwitterStartAuthFlowPath(TwitterAuthHelper helper) {
        this.helper = helper;
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
                exchange.getResponseHeaders().add(Headers.LOCATION, helper.authorizationUrl());
            } catch (Throwable ex) {
                ex.printStackTrace();
                throw ex;
            }

            exchange.setStatusCode(StatusCodes.TEMPORARY_REDIRECT);
        } else {
            exchange.setStatusCode(StatusCodes.METHOD_NOT_ALLOWED);
        }
    }
}
