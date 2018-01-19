package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.BaseJsonHandler;
import net.socialgamer.cah.servlets.Parameters;

public abstract class BaseHandler {
    public abstract JsonElement handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException;
}
