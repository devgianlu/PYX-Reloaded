package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.data.Game;
import com.gianlu.pyxreloaded.data.GameManager;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.Annotations;
import com.gianlu.pyxreloaded.servlets.Parameters;
import io.undertow.server.HttpServerExchange;

public class DislikeGameHandler extends GameHandler {
    public static final String OP = Consts.Operation.DISLIKE.toString();

    public DislikeGameHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonWrapper handle(User user, Game game, Parameters params, HttpServerExchange exchange) {
        game.toggleDislikeGame(user);
        return game.getLikesInfoJson(user);
    }
}
