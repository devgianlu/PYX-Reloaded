package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Consts;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Parameters;

/**
 * Class to get user's nick for the game.html page - not safe to store/retrieve as a cookie.
 * This class returns a JSON string containing the user's nick to the client through AJAX.
 * More data will be added/used once user accounts are added.
 **/
public class GetMeHandler extends BaseHandler {
    public static final String OP = Consts.Operation.ME.toString();

    public GetMeHandler() {
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) {
        return new JsonWrapper(Consts.GeneralKeys.NICKNAME, user.getNickname());
    }
}
