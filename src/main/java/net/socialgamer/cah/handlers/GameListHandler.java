package net.socialgamer.cah.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.GameManager.MaxGames;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Parameters;

public class GameListHandler extends BaseHandler {
    public static final String OP = AjaxOperation.GAME_LIST.toString();
    private final GameManager gameManager;
    private final int maxGames;

    @Inject
    public GameListHandler(final GameManager gameManager, @MaxGames final Integer maxGames) {
        this.gameManager = gameManager;
        this.maxGames = maxGames;
    }

    @Override
    public JsonElement handle(User user, Parameters params, NanoHTTPD.IHTTPSession session) {
        JsonObject json = new JsonObject();

        JsonArray infoArray = new JsonArray();
        for (Game game : gameManager.getGameList()) {
            JsonObject info = game.getInfoJson(false);
            if (info != null) infoArray.add(info);
        }

        json.add(AjaxResponse.GAMES.toString(), infoArray);
        json.addProperty(AjaxResponse.MAX_GAMES.toString(), maxGames);
        return json;
    }
}
