package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.GamesManager;
import io.undertow.server.HttpServerExchange;

public class GetGameInfoHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.GET_GAME_INFO.toString();

    public GetGameInfoHandler(@Annotations.GameManager GamesManager gamesManager) {
        super(gamesManager);
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.GameInfoData.INFO, game.getInfoJson(user, true));
        obj.add(Consts.GamePlayerInfo.INFO, game.getAllPlayersInfoJson());
        return obj;
    }
}
