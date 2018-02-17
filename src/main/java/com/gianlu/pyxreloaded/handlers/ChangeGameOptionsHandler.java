package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.Preferences;
import com.gianlu.pyxreloaded.data.*;
import com.gianlu.pyxreloaded.servlets.Annotations;
import com.gianlu.pyxreloaded.servlets.BaseCahHandler;
import com.gianlu.pyxreloaded.servlets.Parameters;
import io.undertow.server.HttpServerExchange;

public class ChangeGameOptionsHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.CHANGE_GAME_OPTIONS.toString();
    private final Preferences preferences;

    public ChangeGameOptionsHandler(@Annotations.GameManager GameManager gameManager, @Annotations.Preferences Preferences preferences) {
        super(gameManager);
        this.preferences = preferences;
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (game.getState() != Consts.GameState.LOBBY)
            throw new BaseCahHandler.CahException(Consts.ErrorCode.ALREADY_STARTED);

        User host = game.getHost();
        if (host == null) return JsonWrapper.EMPTY;

        String value = params.get(Consts.GameOptionsData.OPTIONS);
        if (value == null || value.isEmpty()) throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

        if (host == user) {
            game.updateGameSettings(new GameOptions(preferences, value));
            return JsonWrapper.EMPTY;
        } else {
            game.suggestGameOptionsModification(new SuggestedGameOptions(preferences, user, value));
            return new JsonWrapper(Consts.GameInfoData.HOST, host.getNickname());
        }
    }
}
