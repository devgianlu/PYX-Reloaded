package net.socialgamer.cah.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.Utils;
import net.socialgamer.cah.cardcast.CardcastDeck;
import net.socialgamer.cah.cardcast.CardcastService;
import net.socialgamer.cah.cardcast.FailedLoadingSomeCardcastDecks;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.BaseCahHandler;
import net.socialgamer.cah.servlets.Parameters;

public class CardcastListCardsetsHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.CARDCAST_LIST_CARDSETS.toString();
    private final CardcastService cardcastService;

    public CardcastListCardsetsHandler(@Annotations.GameManager GameManager gameManager, @Annotations.CardcastService CardcastService cardcastService) {
        super(gameManager);
        this.cardcastService = cardcastService;
    }

    @Override
    public JsonElement handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        JsonArray array = new JsonArray();

        FailedLoadingSomeCardcastDecks cardcastException = null;
        for (String deckId : game.getCardcastDeckCodes().toArray(new String[0])) {
            CardcastDeck deck = cardcastService.loadSet(deckId);
            if (deck == null) {
                if (cardcastException == null) cardcastException = new FailedLoadingSomeCardcastDecks();
                cardcastException.failedDecks.add(deckId);
            }

            if (deck != null) array.add(deck.getClientMetadataJson());
        }

        if (cardcastException != null) {
            throw new BaseCahHandler.CahException(ErrorCode.CARDCAST_CANNOT_FIND,
                    Utils.singletonJsonObject(Constants.AjaxResponse.CARDCAST_ID.toString(),
                            Utils.singletonJsonArray(cardcastException.getFailedJson())));
        } else {
            return Utils.singletonJsonObject(AjaxResponse.CARD_SETS.toString(), array);
        }
    }
}
