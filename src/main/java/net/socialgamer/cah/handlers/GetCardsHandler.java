package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.Parameters;

public class GetCardsHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.GET_CARDS.toString();

    public GetCardsHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) {
        JsonWrapper obj = new JsonWrapper();
        obj.add(AjaxResponse.HAND, game.getHandJson(user));
        obj.add(AjaxResponse.BLACK_CARD, game.getBlackCardJson());
        obj.add(AjaxResponse.WHITE_CARDS, game.getWhiteCardsJson(user));
        obj.add(AjaxResponse.GAME_ID, game.getId());

        return obj;
    }
}
