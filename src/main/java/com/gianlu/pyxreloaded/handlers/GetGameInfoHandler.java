package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.data.Game;
import com.gianlu.pyxreloaded.data.GameManager;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.Annotations;
import com.gianlu.pyxreloaded.servlets.Parameters;
import io.undertow.server.HttpServerExchange;

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
