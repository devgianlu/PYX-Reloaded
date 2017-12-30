package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.BaseUriResponder;
import net.socialgamer.cah.servlets.Parameters;


public abstract class BaseHandler {
    public abstract JsonElement handle(User user, Parameters params, NanoHTTPD.IHTTPSession session) throws BaseUriResponder.StatusException;
}
