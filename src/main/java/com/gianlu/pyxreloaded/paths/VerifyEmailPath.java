package com.gianlu.pyxreloaded.paths;

import com.gianlu.pyxreloaded.Utils;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.singletons.Emails;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.apache.log4j.Logger;

import java.sql.SQLException;

public class VerifyEmailPath implements HttpHandler {
    private static final Logger logger = Logger.getLogger(VerifyEmailPath.class);
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
                exchange.getResponseSender().send("Missing token!");
                return;
            }

            exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/html");

            try {
                emails.tryVerify(token);
                exchange.setStatusCode(StatusCodes.OK);
                exchange.getResponseSender().send("<meta http-equiv=\"refresh\" content=\"3;url=/\" />Your email has been verified. You'll be redirected in a few seconds...");
            } catch (SQLException ex) {
                exchange.setStatusCode(StatusCodes.FORBIDDEN);
                exchange.getResponseSender().send("Invalid token. Failed to verify your email or already verified.");
            } catch (BaseCahHandler.CahException ex) {
                exchange.setStatusCode(StatusCodes.BAD_REQUEST);
                exchange.getResponseSender().send(ex.getMessage());
            }
        } catch (Throwable ex) {
            logger.error("Failed verifying email.", ex);
            throw ex;
        }
    }
}
