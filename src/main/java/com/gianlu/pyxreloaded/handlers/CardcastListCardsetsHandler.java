package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.cardcast.CardcastDeck;
import com.gianlu.pyxreloaded.cardcast.CardcastService;
import com.gianlu.pyxreloaded.cardcast.FailedLoadingSomeCardcastDecks;
import com.gianlu.pyxreloaded.data.Game;
import com.gianlu.pyxreloaded.data.GameManager;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.Annotations;
import com.gianlu.pyxreloaded.servlets.BaseCahHandler;
import com.gianlu.pyxreloaded.servlets.Parameters;
import com.google.gson.JsonArray;
import io.undertow.server.HttpServerExchange;

public class CardcastListCardsetsHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.CARDCAST_LIST_CARDSETS.toString();
    private final CardcastService cardcastService;

    public CardcastListCardsetsHandler(@Annotations.GameManager GameManager gameManager, @Annotations.CardcastService CardcastService cardcastService) {
        super(gameManager);
        this.cardcastService = cardcastService;
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        JsonArray array = new JsonArray();

        FailedLoadingSomeCardcastDecks cardcastException = null;
        for (String deckId : game.getCardcastDeckCodes().toArray(new String[0])) {
            CardcastDeck deck = cardcastService.loadSet(deckId);
            if (deck == null) {
                if (cardcastException == null) cardcastException = new FailedLoadingSomeCardcastDecks();
                cardcastException.failedDecks.add(deckId);
            }

            if (deck != null) array.add(deck.getClientMetadataJson().obj());
        }

        if (cardcastException != null) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.CARDCAST_CANNOT_FIND,
                    new JsonWrapper(Consts.GeneralKeys.CARDCAST_ID, cardcastException.getFailedJson()));
        } else {
            return new JsonWrapper(Consts.GameOptionsData.CARD_SETS, array);
        }
    }
}
