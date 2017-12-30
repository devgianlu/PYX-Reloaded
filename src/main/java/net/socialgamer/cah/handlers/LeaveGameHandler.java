package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Parameters;


public class LeaveGameHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.LEAVE_GAME.toString();

    public LeaveGameHandler(final GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonElement handleWithUserInGame(User user, Game game, Parameters params, NanoHTTPD.IHTTPSession session) {
        game.removePlayer(user);
        game.removeSpectator(user);
        return new JsonObject();
    }
}
