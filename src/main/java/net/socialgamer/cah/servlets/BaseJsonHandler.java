package net.socialgamer.cah.servlets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import net.socialgamer.cah.Constants;

import java.nio.charset.Charset;
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
                    JsonObject obj = new JsonObject();
                    obj.addProperty(Constants.AjaxResponse.ERROR.toString(), true);
                    obj.addProperty(Constants.AjaxResponse.ERROR_CODE.toString(), ((BaseCahHandler.CahException) ex).code.toString());

                    JsonObject data = ((BaseCahHandler.CahException) ex).data;
                    if (data != null) {
                        for (String key : data.keySet()) obj.add(key, data.get(key));
                    }

                    exchange.getResponseSender().send(obj.toString());
                }
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
