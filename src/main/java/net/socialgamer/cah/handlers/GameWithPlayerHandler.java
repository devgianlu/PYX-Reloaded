package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;

public abstract class GameWithPlayerHandler extends GameHandler {

    public GameWithPlayerHandler(final GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public final JsonElement handle(final User user, final Game game, Parameters params, NanoHTTPD.IHTTPSession session) throws CahResponder.CahException {
        if (user.getGame() != game) throw new CahResponder.CahException(ErrorCode.NOT_IN_THAT_GAME);
        else return handleWithUserInGame(user, game, params, session);
    }

    public abstract JsonElement handleWithUserInGame(User user, Game game, Parameters params, NanoHTTPD.IHTTPSession session) throws CahResponder.CahException;
}
