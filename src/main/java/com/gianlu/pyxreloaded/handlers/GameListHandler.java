package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.game.GameManager;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.Parameters;
import com.google.gson.JsonArray;
import io.undertow.server.HttpServerExchange;

public class GameListHandler extends BaseHandler {
    public static final String OP = Consts.Operation.GAME_LIST.toString();
    private final GameManager gameManager;
    private final int maxGames;

    public GameListHandler(@Annotations.GameManager GameManager gameManager, @Annotations.MaxGames int maxGames) {
        this.gameManager = gameManager;
        this.maxGames = maxGames;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) {
        JsonWrapper json = new JsonWrapper();

        JsonArray infoArray = new JsonArray();
        for (Game game : gameManager.getGameList()) {
            JsonWrapper info = game.getInfoJson(user, false);
            if (info != null) infoArray.add(info.obj());
        }

        json.add(Consts.GeneralKeys.GAMES, infoArray);
        json.add(Consts.GeneralKeys.MAX_GAMES, maxGames);
        return json;
    }
}
