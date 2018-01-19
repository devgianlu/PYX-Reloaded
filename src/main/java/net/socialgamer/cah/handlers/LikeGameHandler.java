package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.Parameters;

public class LikeGameHandler extends GameHandler {
    public static final String OP = Constants.AjaxOperation.LIKE.toString();

    public LikeGameHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonElement handle(User user, Game game, Parameters params, HttpServerExchange exchange) {
        game.toggleLikeGame(user);
        return game.getLikesInfoJson(user);
    }
}
