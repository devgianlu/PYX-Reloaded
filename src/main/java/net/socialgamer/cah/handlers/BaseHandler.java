package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.BaseUriResponder;
import net.socialgamer.cah.servlets.Parameters;

import java.util.HashMap;
import java.util.Map;


public abstract class BaseHandler {
    public final Map<String, String> headers = new HashMap<>();

    public abstract JsonElement handle(User user, Parameters params, NanoHTTPD.IHTTPSession session) throws BaseUriResponder.StatusException;

    protected void setHeader(String name, String value) {
        headers.put(name, value);
    }
}
