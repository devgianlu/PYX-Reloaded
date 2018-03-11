package com.gianlu.pyxreloaded.paths;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.handlers.BaseHandler;
import com.gianlu.pyxreloaded.handlers.Handlers;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.Providers;
import com.google.gson.JsonElement;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class AjaxPath extends BaseCahHandler {
    private final Map<Class<? extends BaseHandler>, BaseHandler> handlers = new HashMap<>();

    @Override
    protected JsonElement handleRequest(@Nullable String op, @Nullable User user, Parameters params, HttpServerExchange exchange) throws StatusException {
        if (user != null) user.userDidSomething();
        if (op == null || op.isEmpty()) throw new CahException(Consts.ErrorCode.OP_NOT_SPECIFIED);

        BaseHandler handler;
        Class<? extends BaseHandler> cls = Handlers.LIST.get(op);
        if (cls != null) {
            handler = handlers.get(cls);
            if (handler == null) {
                try {
                    Constructor<?> constructor = cls.getConstructors()[0];
                    Parameter[] parameters = constructor.getParameters();
                    Object[] objects = new Object[parameters.length];

                    for (int i = 0; i < parameters.length; i++)
                        objects[i] = Providers.get(parameters[i].getAnnotations()[0].annotationType()).get();

                    handler = (BaseHandler) constructor.newInstance(objects);
                    handlers.put(cls, handler);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                    throw new CahException(Consts.ErrorCode.BAD_OP, ex);
                }
            }
        } else {
            throw new CahException(Consts.ErrorCode.BAD_OP);
        }

        return handler.handle(user, params, exchange).obj();
    }
}
