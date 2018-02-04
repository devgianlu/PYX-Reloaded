package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Consts;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.BaseCahHandler;
import net.socialgamer.cah.servlets.Parameters;
import org.apache.commons.lang3.StringEscapeUtils;

public class PlayCardHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.PLAY_CARD.toString();

    public PlayCardHandler(@Annotations.GameManager GameManager gameManager) {
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

        String text = params.get(Consts.GeneralKeys.WRITE_IN_TEXT);
        if (text != null && text.contains("<")) text = StringEscapeUtils.escapeXml11(text);

        return new JsonWrapper(Consts.OngoingGameData.LEFT_TO_PLAY, game.playCard(user, cardId, text));
    }
}
