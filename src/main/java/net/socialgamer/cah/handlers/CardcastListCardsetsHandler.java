package net.socialgamer.cah.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.Utils;
import net.socialgamer.cah.cardcast.CardcastDeck;
import net.socialgamer.cah.cardcast.CardcastService;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;

public class CardcastListCardsetsHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.CARDCAST_LIST_CARDSETS.toString();
    private final CardcastService cardcastService;

    public CardcastListCardsetsHandler(@Annotations.GameManager GameManager gameManager, @Annotations.CardcastService CardcastService cardcastService) {
        super(gameManager);
        this.cardcastService = cardcastService;
    }

    @Override
    public JsonElement handleWithUserInGame(User user, Game game, Parameters params, NanoHTTPD.IHTTPSession session) throws CahResponder.CahException {
        JsonArray array = new JsonArray();
        for (final String deckId : game.getCardcastDeckIds().toArray(new String[0])) {
            final CardcastDeck deck = cardcastService.loadSet(deckId);
            // FIXME we need a way to tell the user which one is broken.
            if (deck == null) throw new CahResponder.CahException(ErrorCode.CARDCAST_CANNOT_FIND);
            array.add(deck.getClientMetadataJson());
        }

        return Utils.singletonJsonObject(AjaxResponse.CARD_SETS.toString(), array);
    }
}
