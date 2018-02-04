package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Consts;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.Parameters;

public class GetCardsHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.GET_CARDS.toString();

    public GetCardsHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.OngoingGameData.HAND, game.getHandJson(user));
        obj.add(Consts.OngoingGameData.BLACK_CARD, game.getBlackCardJson());
        obj.add(Consts.OngoingGameData.WHITE_CARDS, game.getWhiteCardsJson(user));
        obj.add(Consts.GeneralKeys.GAME_ID, game.getId());

        return obj;
    }
}
