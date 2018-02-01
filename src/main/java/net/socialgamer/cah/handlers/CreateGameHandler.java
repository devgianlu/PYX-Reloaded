package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.Preferences;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.GameOptions;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.BaseJsonHandler;
import net.socialgamer.cah.servlets.Parameters;

public class CreateGameHandler extends BaseHandler {
    public static final String OP = AjaxOperation.CREATE_GAME.toString();
    private final Preferences preferences;
    private final GameManager gameManager;

    public CreateGameHandler(@Annotations.Preferences Preferences preferences, @Annotations.GameManager GameManager gameManager) {
        this.preferences = preferences;
        this.gameManager = gameManager;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        String value = params.get(Constants.AjaxRequest.GAME_OPTIONS);
        GameOptions options = GameOptions.deserialize(preferences, value);
        return new JsonWrapper(AjaxResponse.GAME_ID, gameManager.createGameWithPlayer(user, options).getId());
    }
}
