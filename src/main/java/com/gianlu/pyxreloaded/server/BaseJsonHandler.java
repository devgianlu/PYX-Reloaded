package com.gianlu.pyxreloaded.server;

import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.google.gson.JsonElement;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseJsonHandler implements HttpHandler {
    protected final static Logger logger = Logger.getLogger(BaseJsonHandler.class.getSimpleName());

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        if (exchange.getRequestMethod().equalToString("POST")) {
            exchange.startBlocking();
            if (exchange.isInIoThread()) {
                exchange.dispatch(this);
                return;
            }

            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");

            try {
                JsonElement json = handle(exchange);
                exchange.setStatusCode(StatusCodes.OK);
                exchange.getResponseSender().send(json.toString(), Charset.forName("UTF-8"));
            } catch (StatusException ex) {
                exchange.setStatusCode(ex.status);

                if (ex instanceof BaseCahHandler.CahException) {
                    JsonWrapper obj = new JsonWrapper(((BaseCahHandler.CahException) ex).code);
                    obj.addAll(((BaseCahHandler.CahException) ex).data);
                    exchange.getResponseSender().send(obj.toString());
                }
            } catch (Throwable ex) {
                logger.log(Level.SEVERE, "Failed processing the request: " + exchange, ex);
            }
        } else {
            exchange.setStatusCode(StatusCodes.METHOD_NOT_ALLOWED);
        }
    }

    protected abstract JsonElement handle(HttpServerExchange exchange) throws StatusException;

    public static class StatusException extends Exception {
        private final int status;

        StatusException(int status) {
            super(status + ": " + StatusCodes.getReason(status));
            this.status = status;
        }

        StatusException(int status, Throwable cause) {
            super(status + ": " + StatusCodes.getReason(status), cause);
            cause.printStackTrace();
            this.status = status;
        }
    }
}
