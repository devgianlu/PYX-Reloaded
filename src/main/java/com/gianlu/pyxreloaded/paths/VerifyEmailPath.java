package com.gianlu.pyxreloaded.paths;

import com.gianlu.pyxreloaded.Utils;
import com.gianlu.pyxreloaded.singletons.Emails;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

import java.util.logging.Level;
import java.util.logging.Logger;

public class VerifyEmailPath implements HttpHandler {
    private static final Logger logger = Logger.getLogger(VerifyEmailPath.class.getSimpleName());
    private final Emails emails;

    public VerifyEmailPath(Emails emails) {
        this.emails = emails;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        exchange.startBlocking();
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
            return;
        }

        try {
            String token = Utils.extractParam(exchange, "token");
            if (token == null) {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                return;
            }

            exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/html");

            if (emails.tryVerify(token)) {
                exchange.setStatusCode(StatusCodes.OK);
                exchange.getResponseSender().send("<meta http-equiv=\"refresh\" content=\"3;url=/\" />Your email has been verified. You'll be redirected in a few seconds...");
            } else {
                exchange.setStatusCode(StatusCodes.FORBIDDEN);
                exchange.getResponseSender().send("Invalid token. Failed to verify your email or already verified.");
            }
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Failed verifying email: ", ex);
            throw ex;
        }
    }
}
