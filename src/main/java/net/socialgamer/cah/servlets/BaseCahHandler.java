package net.socialgamer.cah.servlets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.StatusCodes;
import net.socialgamer.cah.Consts;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.User;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public abstract class BaseCahHandler extends BaseJsonHandler {

    @Override
    protected JsonElement handle(HttpServerExchange exchange) throws StatusException {
        Cookie sid = exchange.getRequestCookies().get("PYX-Session");
        User user = null;
        if (sid != null) user = Sessions.getUser(sid.getValue());

        Parameters params;
        try {
            params = Parameters.fromExchange(exchange, (int) exchange.getRequestContentLength());
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new StatusException(StatusCodes.INTERNAL_SERVER_ERROR, ex);
        }

        String op = params.get(Consts.GeneralKeys.OP);
        boolean skipUserCheck = Objects.equals(op, Consts.Operation.REGISTER.toString()) || Objects.equals(op, Consts.Operation.FIRST_LOAD.toString());
        if (!skipUserCheck && user == null) {
            throw new CahException(Consts.ErrorCode.NOT_REGISTERED);
        } else if (user != null && !user.isValid()) {
            Sessions.invalidate(sid.getValue());
            throw new CahException(Consts.ErrorCode.SESSION_EXPIRED);
        } else {
            return handleRequest(op, user, params, exchange);
        }
    }

    protected abstract JsonElement handleRequest(@Nullable String op, @Nullable User user, Parameters params, HttpServerExchange exchange) throws StatusException;

    public static class CahException extends StatusException {
        public final Consts.ErrorCode code;
        public final JsonObject data;

        public CahException(Consts.ErrorCode code) {
            super(StatusCodes.INTERNAL_SERVER_ERROR);
            this.code = code;
            this.data = null;
        }

        public CahException(Consts.ErrorCode code, Throwable cause) {
            super(StatusCodes.INTERNAL_SERVER_ERROR, cause);
            this.code = code;
            this.data = null;
        }

        public CahException(Consts.ErrorCode code, JsonWrapper data) {
            super(StatusCodes.INTERNAL_SERVER_ERROR);
            this.code = code;
            this.data = data.obj();
        }
    }
}
