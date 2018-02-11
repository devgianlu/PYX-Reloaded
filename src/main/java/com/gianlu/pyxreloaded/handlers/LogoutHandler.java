package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.data.ConnectedUsers;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.Annotations;
import com.gianlu.pyxreloaded.servlets.Parameters;
import com.gianlu.pyxreloaded.servlets.Sessions;
import io.undertow.server.HttpServerExchange;

public class LogoutHandler extends BaseHandler {
    public final static String OP = Consts.Operation.LOG_OUT.toString();
    private final ConnectedUsers users;

    public LogoutHandler(@Annotations.ConnectedUsers ConnectedUsers users) {
        this.users = users;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) {
        user.noLongerValid();
        users.removeUser(user, Consts.DisconnectReason.MANUAL);
        Sessions.invalidate(user.getSessionId());
        return JsonWrapper.EMPTY;
    }
}
