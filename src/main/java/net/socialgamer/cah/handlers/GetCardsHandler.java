package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Parameters;

public class GetCardsHandler extends GameWithPlayerHandler {

    public static final String OP = AjaxOperation.GET_CARDS.toString();

    @Inject
    public GetCardsHandler(final GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonElement handleWithUserInGame(User user, Game game, Parameters params, NanoHTTPD.IHTTPSession session) {
        JsonObject obj = new JsonObject();
        obj.add(AjaxResponse.HAND.toString(), game.getHandJson(user));
        obj.add(AjaxResponse.BLACK_CARD.toString(), game.getBlackCardJson());
        obj.add(AjaxResponse.WHITE_CARDS.toString(), game.getWhiteCardsJson(user));
        obj.addProperty(AjaxResponse.GAME_ID.toString(), game.getId());

        return obj;
    }
}
