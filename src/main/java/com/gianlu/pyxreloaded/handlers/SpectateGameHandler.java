package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.GamesManager;
import com.gianlu.pyxreloaded.singletons.PreparingShutdown;
import io.undertow.server.HttpServerExchange;

public class SpectateGameHandler extends GameHandler {
    public static final String OP = Consts.Operation.SPECTATE_GAME.toString();

    public SpectateGameHandler(@Annotations.GameManager GamesManager gamesManager) {
        super(gamesManager);
    }

    @Override
    public JsonWrapper handle(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        PreparingShutdown.check();

        if (!game.isPasswordCorrect(params.getString(Consts.GameOptionsData.PASSWORD)))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.WRONG_PASSWORD);

        game.addSpectator(user);
        return JsonWrapper.EMPTY;
    }
}
