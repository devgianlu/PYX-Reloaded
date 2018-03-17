package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.GameOptions;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseJsonHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.GamesManager;
import com.gianlu.pyxreloaded.singletons.Preferences;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

public class CreateGameHandler extends BaseHandler {
    public static final String OP = Consts.Operation.CREATE_GAME.toString();
    private final Preferences preferences;
    private final GamesManager gamesManager;

    public CreateGameHandler(@Annotations.Preferences Preferences preferences, @Annotations.GameManager GamesManager gamesManager) {
        this.preferences = preferences;
        this.gamesManager = gamesManager;
    }

    @NotNull
    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        String value = params.getStringNotNull(Consts.GameOptionsData.OPTIONS);
        GameOptions options = new GameOptions(preferences, value);
        return new JsonWrapper(Consts.GeneralKeys.GAME_ID, gamesManager.createGameWithPlayer(user, options).getId());
    }
}
