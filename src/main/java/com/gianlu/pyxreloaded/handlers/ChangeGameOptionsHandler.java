package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.game.GameOptions;
import com.gianlu.pyxreloaded.game.SuggestedGameOptions;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.GamesManager;
import com.gianlu.pyxreloaded.singletons.Preferences;
import io.undertow.server.HttpServerExchange;

public class ChangeGameOptionsHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.CHANGE_GAME_OPTIONS.toString();
    private final Preferences preferences;

    public ChangeGameOptionsHandler(@Annotations.GameManager GamesManager gamesManager, @Annotations.Preferences Preferences preferences) {
        super(gamesManager);
        this.preferences = preferences;
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (game.getState() != Consts.GameState.LOBBY)
            throw new BaseCahHandler.CahException(Consts.ErrorCode.ALREADY_STARTED);

        User host = game.getHost();
        if (host == null) return JsonWrapper.EMPTY;

        String value = params.getStringNotNull(Consts.GameOptionsData.OPTIONS);
        if (value.isEmpty()) throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

        if (host == user) {
            game.updateGameSettings(new GameOptions(preferences, value));
            return JsonWrapper.EMPTY;
        } else {
            game.suggestGameOptionsModification(new SuggestedGameOptions(preferences, user, value));
            return new JsonWrapper(Consts.GameInfoData.HOST, host.getNickname());
        }
    }
}
