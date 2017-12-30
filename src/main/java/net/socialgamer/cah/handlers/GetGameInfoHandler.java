package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Parameters;

public class GetGameInfoHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.GET_GAME_INFO.toString();

    public GetGameInfoHandler(final GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonElement handleWithUserInGame(User user, Game game, Parameters params, NanoHTTPD.IHTTPSession session) {
        JsonObject obj = new JsonObject();
        obj.add(AjaxResponse.GAME_INFO.toString(), game.getInfoJson(true));
        obj.add(AjaxResponse.PLAYER_INFO.toString(), game.getAllPlayersInfoJson());
        return obj;
    }
}
