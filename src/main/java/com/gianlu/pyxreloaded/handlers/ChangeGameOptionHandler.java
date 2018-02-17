package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.Preferences;
import com.gianlu.pyxreloaded.data.Game;
import com.gianlu.pyxreloaded.data.GameManager;
import com.gianlu.pyxreloaded.data.GameOptions;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.Annotations;
import com.gianlu.pyxreloaded.servlets.BaseCahHandler;
import com.gianlu.pyxreloaded.servlets.Parameters;
import io.undertow.server.HttpServerExchange;

public class ChangeGameOptionHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.CHANGE_GAME_OPTIONS.toString();
    private final Preferences preferences;

    public ChangeGameOptionHandler(@Annotations.GameManager GameManager gameManager, @Annotations.Preferences Preferences preferences) {
        super(gameManager);
        this.preferences = preferences;
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (game.getState() != Consts.GameState.LOBBY)
            throw new BaseCahHandler.CahException(Consts.ErrorCode.ALREADY_STARTED);

        GameOptions options;
        try {
            String value = params.get(Consts.GameOptionData.OPTIONS);
            options = GameOptions.deserialize(preferences, value);
        } catch (Exception ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST, ex);
        }

        if (game.getHost() == user) {
            game.updateGameSettings(options);
        } else {
            // TODO: Suggest options modification
            throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_GAME_HOST);
        }

        return JsonWrapper.EMPTY;
    }
}
