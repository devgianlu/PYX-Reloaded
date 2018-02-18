package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.data.Game;
import com.gianlu.pyxreloaded.data.GameManager;
import com.gianlu.pyxreloaded.data.SuggestedGameOptions;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.Annotations;
import com.gianlu.pyxreloaded.servlets.BaseCahHandler;
import com.gianlu.pyxreloaded.servlets.Parameters;
import com.google.gson.JsonArray;
import io.undertow.server.HttpServerExchange;

import java.util.Map;

public class GetSuggestedGameOptionsHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.GET_SUGGESTED_GAME_OPTIONS.toString();

    public GetSuggestedGameOptionsHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

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
