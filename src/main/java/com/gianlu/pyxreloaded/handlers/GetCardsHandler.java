package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.data.Game;
import com.gianlu.pyxreloaded.data.GameManager;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.Annotations;
import com.gianlu.pyxreloaded.servlets.Parameters;
import io.undertow.server.HttpServerExchange;

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
