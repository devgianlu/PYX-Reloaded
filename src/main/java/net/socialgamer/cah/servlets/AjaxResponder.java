package net.socialgamer.cah.servlets;

import com.google.gson.JsonElement;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.handlers.BaseHandler;
import net.socialgamer.cah.handlers.Handlers;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

public class AjaxResponder extends CahResponder {

    @Override
    protected JsonElement handleRequest(@NotNull String op, User user, Parameters params, NanoHTTPD.IHTTPSession session) throws StatusException {
        if (user != null) user.userDidSomething();

        BaseHandler handler;
        Class<? extends BaseHandler> cls = Handlers.LIST.get(op);
        if (cls != null) {
            try {
                Constructor<?> constructor = cls.getConstructors()[0];
                Parameter[] parameters = constructor.getParameters();
                Object[] objects = new Object[parameters.length];

                for (int i = 0; i < parameters.length; i++) {
                    Object obj = Providers.get(parameters[i].getAnnotations()[0].annotationType()).get();
                    if (obj.getClass() == parameters[i].getType()) objects[i] = obj;
                    else throw new CahException(Constants.ErrorCode.SERVER_ERROR);
                }

                handler = (BaseHandler) constructor.newInstance(objects);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
                throw new CahException(Constants.ErrorCode.BAD_OP, ex);
            }
        } else {
            throw new CahException(Constants.ErrorCode.BAD_OP);
        }

        return handler.handle(user, params, session);
    }
}
