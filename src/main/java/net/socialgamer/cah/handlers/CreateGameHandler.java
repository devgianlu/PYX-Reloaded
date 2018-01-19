package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.BaseCahHandler;
import net.socialgamer.cah.servlets.BaseJsonHandler;
import net.socialgamer.cah.servlets.Parameters;

public class CreateGameHandler extends BaseHandler {
    public static final String OP = AjaxOperation.CREATE_GAME.toString();
    private final GameManager gameManager;

    public CreateGameHandler(@Annotations.GameManager GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public JsonElement handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        Game game;
        try {
            game = gameManager.createGameWithPlayer(user);
        } catch (IllegalStateException ex) {
            throw new BaseCahHandler.CahException(ErrorCode.CANNOT_JOIN_ANOTHER_GAME, ex);
        }

        if (game == null) {
            throw new BaseCahHandler.CahException(ErrorCode.TOO_MANY_GAMES);
        } else {
            JsonObject obj = new JsonObject();
            obj.addProperty(AjaxResponse.GAME_ID.toString(), game.getId());
            return obj;
        }
    }
}
