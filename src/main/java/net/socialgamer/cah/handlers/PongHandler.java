package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Consts;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Parameters;

public class PongHandler extends BaseHandler {
    public static final String OP = Consts.Operation.PONG.toString();

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) {
        user.userReceivedEvents();
        return JsonWrapper.EMPTY;
    }
}
