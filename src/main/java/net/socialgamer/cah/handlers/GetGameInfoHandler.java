package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.Parameters;

public class GetGameInfoHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.GET_GAME_INFO.toString();

    public GetGameInfoHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) {
        JsonWrapper obj = new JsonWrapper();
        obj.add(AjaxResponse.GAME_INFO, game.getInfoJson(true));
        obj.add(AjaxResponse.PLAYER_INFO, game.getAllPlayersInfoJson());
        return obj;
    }
}
