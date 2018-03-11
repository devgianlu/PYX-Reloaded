package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.GamesManager;
import io.undertow.server.HttpServerExchange;

public abstract class GameHandler extends BaseHandler {
    protected final GamesManager gamesManager;

    public GameHandler(GamesManager gamesManager) {
        this.gamesManager = gamesManager;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        String gameIdStr = params.getStringNotNull(Consts.GeneralKeys.GAME_ID);
        if (gameIdStr.isEmpty()) throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

        int gameId;
        try {
            gameId = Integer.parseInt(gameIdStr);
        } catch (NumberFormatException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.INVALID_GAME, ex);
        }

        final Game game = gamesManager.getGame(gameId);
        if (game == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.INVALID_GAME);

        return handle(user, game, params, exchange);
    }

    public abstract JsonWrapper handle(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException;
}
