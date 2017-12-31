package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxRequest;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;

public abstract class GameHandler extends BaseHandler {
    protected final GameManager gameManager;

    public GameHandler(final GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public JsonElement handle(User user, Parameters params, NanoHTTPD.IHTTPSession session) throws CahResponder.CahException {
        String gameIdStr = params.get(AjaxRequest.GAME_ID);
        if (gameIdStr == null || gameIdStr.isEmpty()) throw new CahResponder.CahException(ErrorCode.NO_GAME_SPECIFIED);

        int gameId;
        try {
            gameId = Integer.parseInt(gameIdStr);
        } catch (final NumberFormatException ex) {
            throw new CahResponder.CahException(ErrorCode.INVALID_GAME, ex);
        }

        final Game game = gameManager.getGame(gameId);
        if (game == null) throw new CahResponder.CahException(ErrorCode.INVALID_GAME);

        return handle(user, game, params, session);
    }

    public abstract JsonElement handle(User user, Game game, Parameters params, NanoHTTPD.IHTTPSession session) throws CahResponder.CahException;
}
