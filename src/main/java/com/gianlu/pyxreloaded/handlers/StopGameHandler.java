package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.data.Game;
import com.gianlu.pyxreloaded.data.GameManager;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.Annotations;
import com.gianlu.pyxreloaded.servlets.BaseCahHandler;
import com.gianlu.pyxreloaded.servlets.Parameters;
import io.undertow.server.HttpServerExchange;
import org.apache.log4j.Logger;

public class StopGameHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.STOP_GAME.toString();
    protected final Logger logger = Logger.getLogger(GameWithPlayerHandler.class);

    public StopGameHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (game.getHost() != user) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_GAME_HOST);
        } else if (game.getState() == Consts.GameState.LOBBY) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.ALREADY_STOPPED);
        } else {
            logger.info(String.format("Game %d stopped by host %s. Players: %s", game.getId(), user, game.getPlayers()));
            game.resetState(false);
            return JsonWrapper.EMPTY;
        }
    }
}
