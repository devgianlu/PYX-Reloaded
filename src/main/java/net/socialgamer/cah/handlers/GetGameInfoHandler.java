package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Consts;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.Parameters;

public class GetGameInfoHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.GET_GAME_INFO.toString();

    public GetGameInfoHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.GameInfoData.INFO, game.getInfoJson(user, true));
        obj.add(Consts.GamePlayerInfo.INFO, game.getAllPlayersInfoJson());
        return obj;
    }
}
