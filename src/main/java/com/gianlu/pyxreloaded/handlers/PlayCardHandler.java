package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.game.GameManager;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import io.undertow.server.HttpServerExchange;
import org.apache.commons.lang3.StringEscapeUtils;

public class PlayCardHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.PLAY_CARD.toString();

    public PlayCardHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
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

        String text = params.getString(Consts.GeneralKeys.WRITE_IN_TEXT);
        if (text != null && text.contains("<")) text = StringEscapeUtils.escapeXml11(text);

        return game.playCard(user, cardId, text);
    }
}
