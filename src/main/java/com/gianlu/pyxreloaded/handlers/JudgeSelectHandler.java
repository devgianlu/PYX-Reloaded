package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.GamesManager;
import io.undertow.server.HttpServerExchange;

public class JudgeSelectHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.JUDGE_SELECT.toString();

    public JudgeSelectHandler(@Annotations.GameManager GamesManager gamesManager) {
        super(gamesManager);
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        String cardIdStr = params.getStringNotNull(Consts.GeneralKeys.CARD_ID);
        if (cardIdStr.isEmpty()) throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

        int cardId;
        try {
            cardId = Integer.parseInt(cardIdStr);
        } catch (NumberFormatException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.INVALID_CARD, ex);
        }

        game.judgeCard(user, cardId);
        return JsonWrapper.EMPTY;
    }
}
