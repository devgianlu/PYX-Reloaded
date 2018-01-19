package net.socialgamer.cah.servlets;

import com.google.gson.JsonElement;
import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.handlers.BaseHandler;
import net.socialgamer.cah.handlers.Handlers;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

public class BaseAjaxHandler extends BaseCahHandler {

    @Override
    protected JsonElement handleRequest(@Nullable String op, @Nullable User user, Parameters params, HttpServerExchange exchange) throws StatusException {
        if (user != null) user.userDidSomething();
        if (op == null || op.isEmpty()) throw new CahException(Constants.ErrorCode.OP_NOT_SPECIFIED);

        BaseHandler handler;
        Class<? extends BaseHandler> cls = Handlers.LIST.get(op);
        if (cls != null) {
            try {
                Constructor<?> constructor = cls.getConstructors()[0];
                Parameter[] parameters = constructor.getParameters();
                Object[] objects = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++)
                    objects[i] = Providers.get(parameters[i].getAnnotations()[0].annotationType()).get();

                handler = (BaseHandler) constructor.newInstance(objects);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                throw new CahException(Constants.ErrorCode.BAD_OP, ex);
            }
        } else {
            throw new CahException(Constants.ErrorCode.BAD_OP);
        }

        return handler.handle(user, params, exchange).obj();
    }
}
