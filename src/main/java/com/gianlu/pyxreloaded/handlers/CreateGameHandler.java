package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.Preferences;
import com.gianlu.pyxreloaded.data.GameManager;
import com.gianlu.pyxreloaded.data.GameOptions;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.Annotations;
import com.gianlu.pyxreloaded.servlets.BaseJsonHandler;
import com.gianlu.pyxreloaded.servlets.Parameters;
import io.undertow.server.HttpServerExchange;

public class CreateGameHandler extends BaseHandler {
    public static final String OP = Consts.Operation.CREATE_GAME.toString();
    private final Preferences preferences;
    private final GameManager gameManager;

    public CreateGameHandler(@Annotations.Preferences Preferences preferences, @Annotations.GameManager GameManager gameManager) {
        this.preferences = preferences;
        this.gameManager = gameManager;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        String value = params.get(Consts.GameOptionsData.OPTIONS);
        GameOptions options = new GameOptions(preferences, value);
        return new JsonWrapper(Consts.GeneralKeys.GAME_ID, gameManager.createGameWithPlayer(user, options).getId());
    }
}
