package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.data.Game;
import com.gianlu.pyxreloaded.data.GameManager;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.Annotations;
import com.gianlu.pyxreloaded.servlets.BaseCahHandler;
import com.gianlu.pyxreloaded.servlets.Parameters;
import io.undertow.server.HttpServerExchange;

public class JudgeSelectHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.JUDGE_SELECT.toString();

    public JudgeSelectHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        String cardIdStr = params.get(Consts.GeneralKeys.CARD_ID);
        if (cardIdStr == null || cardIdStr.isEmpty())
            throw new BaseCahHandler.CahException(Consts.ErrorCode.NO_CARD_SPECIFIED);

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
