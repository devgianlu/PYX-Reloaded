package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.data.Game;
import com.gianlu.pyxreloaded.data.GameManager;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.Annotations;
import com.gianlu.pyxreloaded.servlets.Parameters;
import io.undertow.server.HttpServerExchange;

public class LikeGameHandler extends GameHandler {
    public static final String OP = Consts.Operation.LIKE.toString();

    public LikeGameHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonWrapper handle(User user, Game game, Parameters params, HttpServerExchange exchange) {
        game.toggleLikeGame(user);
        return game.getLikesInfoJson(user);
    }
}
