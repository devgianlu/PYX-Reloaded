package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.game.SuggestedGameOptions;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.GamesManager;
import com.google.gson.JsonArray;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class GetSuggestedGameOptionsHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.GET_SUGGESTED_GAME_OPTIONS.toString();

    public GetSuggestedGameOptionsHandler(@Annotations.GameManager GamesManager gamesManager) {
        super(gamesManager);
    }

    @NotNull
    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (user != game.getHost()) throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_GAME_HOST);

        JsonWrapper obj = new JsonWrapper();
        JsonArray array = new JsonArray();
        for (Map.Entry<String, SuggestedGameOptions> entry : game.getSuggestedGameOptions().entrySet())
            array.add(entry.getValue().toJson(entry.getKey(), true).obj());

        obj.add(Consts.GameSuggestedOptionsData.OPTIONS, array);
        return obj;
    }
}
