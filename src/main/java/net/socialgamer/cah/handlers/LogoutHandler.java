package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Consts;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.Parameters;
import net.socialgamer.cah.servlets.Sessions;

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
