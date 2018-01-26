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
