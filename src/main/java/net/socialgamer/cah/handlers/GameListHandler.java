package net.socialgamer.cah.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.Parameters;

public class GameListHandler extends BaseHandler {
    public static final String OP = AjaxOperation.GAME_LIST.toString();
    private final GameManager gameManager;
    private final int maxGames;

    public GameListHandler(@Annotations.GameManager GameManager gameManager, @Annotations.MaxGames int maxGames) {
        this.gameManager = gameManager;
        this.maxGames = maxGames;
    }

    @Override
    public JsonElement handle(User user, Parameters params, HttpServerExchange exchange) {
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
