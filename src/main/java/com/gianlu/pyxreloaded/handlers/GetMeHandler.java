package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.server.Parameters;
import io.undertow.server.HttpServerExchange;

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
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.GeneralKeys.NICKNAME, user.getNickname());

        if (user.getGame() != null) obj.add(Consts.GeneralKeys.GAME_ID, user.getGame().getId());
        else obj.add(Consts.GeneralKeys.GAME_ID, -1);

        return obj;
    }
}
