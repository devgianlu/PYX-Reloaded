package com.gianlu.pyxreloaded.paths;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.Handlers;
import com.google.gson.JsonElement;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.Nullable;

public class AjaxPath extends BaseCahHandler {

    @Override
    protected JsonElement handleRequest(@Nullable String op, @Nullable User user, Parameters params, HttpServerExchange exchange) throws StatusException {
        if (user != null) user.userDidSomething();
        if (op == null || op.isEmpty()) throw new CahException(Consts.ErrorCode.OP_NOT_SPECIFIED);
        return Handlers.obtain(op).handle(user, params, exchange).obj();
    }
}
