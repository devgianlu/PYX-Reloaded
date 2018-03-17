package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.ConnectedUsers;
import com.gianlu.pyxreloaded.singletons.Sessions;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

public class LogoutHandler extends BaseHandler {
    public final static String OP = Consts.Operation.LOG_OUT.toString();
    private final ConnectedUsers users;

    public LogoutHandler(@Annotations.ConnectedUsers ConnectedUsers users) {
        this.users = users;
    }

    @NotNull
    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) {
        user.noLongerValid();
        users.removeUser(user, Consts.DisconnectReason.MANUAL);
        Sessions.get().invalidate(user.getSessionId());
        return JsonWrapper.EMPTY;
    }
}
