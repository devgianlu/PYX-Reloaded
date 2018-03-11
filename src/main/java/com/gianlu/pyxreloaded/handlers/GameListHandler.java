package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.GamesManager;
import com.google.gson.JsonArray;
import io.undertow.server.HttpServerExchange;

public class GameListHandler extends BaseHandler {
    public static final String OP = Consts.Operation.GAME_LIST.toString();
    private final GamesManager gamesManager;

    public GameListHandler(@Annotations.GameManager GamesManager gamesManager) {
        this.gamesManager = gamesManager;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) {
        JsonWrapper json = new JsonWrapper();

        JsonArray infoArray = new JsonArray();
        for (Game game : gamesManager.getGameList()) {
            JsonWrapper info = game.getInfoJson(user, false);
            if (info != null) infoArray.add(info.obj());
        }

        json.add(Consts.GeneralKeys.GAMES, infoArray);
        json.add(Consts.GeneralKeys.MAX_GAMES, gamesManager.getMaxGames());
        return json;
    }
}
