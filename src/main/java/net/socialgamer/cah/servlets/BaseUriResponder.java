package net.socialgamer.cah.servlets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;
import net.socialgamer.cah.Constants;

import java.util.Map;

public abstract class BaseUriResponder implements RouterNanoHTTPD.UriResponder {

    private static NanoHTTPD.Response methodNotAllowed() {
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, null, null);
    }

    @Override
    public final NanoHTTPD.Response post(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        try {
            JsonElement json = handleRequest(uriResource, new Parameters(session), session); // TODO: Should extract parameters from body
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", json.toString());
        } catch (StatusException ex) {
            if (ex instanceof CahResponder.CahException) {
                JsonObject obj = new JsonObject();
                obj.addProperty(Constants.AjaxResponse.ERROR_CODE.toString(), ((CahResponder.CahException) ex).code.getString());

                JsonObject data = ((CahResponder.CahException) ex).data;
                if (data != null) {
                    for (String key : data.keySet()) obj.add(key, data.get(key));
                }

                return NanoHTTPD.newFixedLengthResponse(ex.status, "application/json", obj.toString()); // TODO: May return additional error info
            }

            return NanoHTTPD.newFixedLengthResponse(ex.status, null, null);
        }
    }

    protected abstract JsonElement handleRequest(RouterNanoHTTPD.UriResource uri, Parameters params, NanoHTTPD.IHTTPSession session) throws StatusException;

    @Override
    public NanoHTTPD.Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return methodNotAllowed();
    }

    @Override
    public NanoHTTPD.Response put(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return methodNotAllowed();
    }

    @Override
    public NanoHTTPD.Response delete(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return methodNotAllowed();
    }

    @Override
    public NanoHTTPD.Response other(String method, RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        return methodNotAllowed();
    }

    public static class StatusException extends Exception {
        private final NanoHTTPD.Response.IStatus status;

        public StatusException(NanoHTTPD.Response.IStatus status) {
            super(status.getRequestStatus() + ": " + status.getDescription());
            this.status = status;
        }

        public StatusException(NanoHTTPD.Response.IStatus status, Throwable cause) {
            super(status.getRequestStatus() + ": " + status.getDescription(), cause);
            this.status = status;
        }
    }
}
