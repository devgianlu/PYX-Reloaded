/*
 * Class to get user's nick for the game.html page - not safe to store/retrieve as a cookie.
 * This class returns a JSON string containing the user's nick to the client through AJAX.
 * More data will be added/used once user accounts are added.
 */
package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Parameters;

public class GetMeHandler extends BaseHandler {
    public static final String OP = Constants.AjaxOperation.ME.toString();

    public GetMeHandler() {
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) {
        return new JsonWrapper(Constants.AjaxResponse.NICKNAME, user.getNickname());
    }
}
