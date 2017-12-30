package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.BaseUriResponder;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;

public class CreateGameHandler extends BaseHandler {
    public static final String OP = AjaxOperation.CREATE_GAME.toString();
    private final GameManager gameManager;

    public CreateGameHandler(final GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public JsonElement handle(User user, Parameters params, NanoHTTPD.IHTTPSession session) throws BaseUriResponder.StatusException {
        Game game;
        try {
            game = gameManager.createGameWithPlayer(user);
        } catch (IllegalStateException ex) {
            throw new CahResponder.CahException(ErrorCode.CANNOT_JOIN_ANOTHER_GAME, ex);
        }

        if (game == null) {
            throw new CahResponder.CahException(ErrorCode.TOO_MANY_GAMES);
        } else {
            JsonObject obj = new JsonObject();
            obj.addProperty(AjaxResponse.GAME_ID.toString(), game.getId());
            return obj;
        }
    }
}
