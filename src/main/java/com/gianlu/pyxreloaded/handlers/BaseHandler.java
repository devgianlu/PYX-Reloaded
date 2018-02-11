package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.BaseJsonHandler;
import com.gianlu.pyxreloaded.servlets.Parameters;
import io.undertow.server.HttpServerExchange;

public abstract class BaseHandler {
    public abstract JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException;
}
