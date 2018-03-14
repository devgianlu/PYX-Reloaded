package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.GamesManager;
import io.undertow.server.HttpServerExchange;

public class GetCardsHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.GET_CARDS.toString();

    public GetCardsHandler(@Annotations.GameManager GamesManager gamesManager) {
        super(gamesManager);
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.OngoingGameData.HAND, game.getHandJson(user));
        obj.addAll(game.getPlayerToPlayCards(user));
        obj.add(Consts.OngoingGameData.BLACK_CARD, game.getBlackCardJson());
        obj.add(Consts.OngoingGameData.WHITE_CARDS, game.getWhiteCardsJson(user));
        obj.add(Consts.GeneralKeys.GAME_ID, game.getId());

        return obj;
    }
}
