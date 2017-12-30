package net.socialgamer.cah.servlets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.data.User;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class CahResponder extends BaseUriResponder {
    @Override
    protected JsonElement handleRequest(RouterNanoHTTPD.UriResource uri, Parameters params, NanoHTTPD.IHTTPSession session) throws StatusException {
        String sid = session.getCookies().read("PYX-Session");
        User user = Sessions.getUser(sid);

        String op = params.getFirst(Constants.AjaxRequest.OP.toString());
        if (op == null || op.isEmpty()) throw new CahException(Constants.ErrorCode.OP_NOT_SPECIFIED);

        boolean skipUserCheck = Objects.equals(op, Constants.AjaxOperation.REGISTER.toString()) || Objects.equals(op, Constants.AjaxOperation.FIRST_LOAD.toString());
        if (!skipUserCheck && user == null) {
            throw new CahException(Constants.ErrorCode.NOT_REGISTERED);
        } else if (user != null && !user.isValid()) {
            Sessions.invalidate(sid);
            throw new CahException(Constants.ErrorCode.SESSION_EXPIRED);
        } else {
            return handleRequest(op, user, params, session);
        }
    }

    protected abstract JsonElement handleRequest(@NotNull String op, User user, Parameters params, NanoHTTPD.IHTTPSession session) throws StatusException;

    public static class CahException extends StatusException {
        public final Constants.ErrorCode code;
        public final JsonObject data;

        public CahException(Constants.ErrorCode code) {
            super(NanoHTTPD.Response.Status.INTERNAL_ERROR);
            this.code = code;
            this.data = null;
        }

        public CahException(Constants.ErrorCode code, Throwable cause) {
            super(NanoHTTPD.Response.Status.INTERNAL_ERROR, cause);
            this.code = code;
            this.data = null;
        }

        public CahException(Constants.ErrorCode code, JsonObject data) {
            super(NanoHTTPD.Response.Status.INTERNAL_ERROR);
            this.code = code;
            this.data = data;
        }
    }
}
