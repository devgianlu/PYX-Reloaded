package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.GamesManager;
import io.undertow.server.HttpServerExchange;

public abstract class GameWithPlayerHandler extends GameHandler {

    public GameWithPlayerHandler(GamesManager gamesManager) {
        super(gamesManager);
    }

    @Override
    public final JsonWrapper handle(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (user.getGame() != game) throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_IN_THAT_GAME);
        else return handleWithUserInGame(user, game, params, exchange);
    }

    public abstract JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException;
}
