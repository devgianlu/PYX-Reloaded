package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxRequest;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;
import org.apache.commons.lang3.StringEscapeUtils;

public class PlayCardHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.PLAY_CARD.toString();

    @Inject
    public PlayCardHandler(final GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonElement handleWithUserInGame(User user, Game game, Parameters params, NanoHTTPD.IHTTPSession session) throws CahResponder.CahException {
        String cardIdStr = params.getFirst(AjaxRequest.CARD_ID);
        if (cardIdStr == null || cardIdStr.isEmpty()) throw new CahResponder.CahException(ErrorCode.NO_CARD_SPECIFIED);

        int cardId;
        try {
            cardId = Integer.parseInt(cardIdStr);
        } catch (NumberFormatException ex) {
            throw new CahResponder.CahException(ErrorCode.INVALID_CARD, ex);
        }

        String text = params.getFirst(AjaxRequest.MESSAGE);
        if (text != null && text.contains("<")) text = StringEscapeUtils.escapeXml11(text);

        final ErrorCode errorCode = game.playCard(user, cardId, text);
        if (errorCode != null) throw new CahResponder.CahException(errorCode);
        else return new JsonObject();
    }
}
