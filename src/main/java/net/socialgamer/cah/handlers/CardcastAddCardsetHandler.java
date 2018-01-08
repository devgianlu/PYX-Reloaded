package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.*;
import net.socialgamer.cah.cardcast.CardcastDeck;
import net.socialgamer.cah.cardcast.CardcastService;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.QueuedMessage.MessageType;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;

public class CardcastAddCardsetHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.CARDCAST_ADD_CARDSET.toString();
    private final CardcastService cardcastService;

    public CardcastAddCardsetHandler(@Annotations.GameManager GameManager gameManager, @Annotations.CardcastService CardcastService cardcastService) {
        super(gameManager);
        this.cardcastService = cardcastService;
    }

    @Override
    public JsonElement handleWithUserInGame(User user, Game game, Parameters params, NanoHTTPD.IHTTPSession session) throws CahResponder.CahException {
        if (game.getHost() != user) throw new CahResponder.CahException(ErrorCode.NOT_GAME_HOST);
        if (game.getState() != GameState.LOBBY) throw new CahResponder.CahException(ErrorCode.ALREADY_STARTED);

        String deckId = params.get(AjaxRequest.CARDCAST_ID);
        if (deckId == null || deckId.isEmpty()) throw new CahResponder.CahException(ErrorCode.BAD_REQUEST);
        if (deckId.length() != 5) throw new CahResponder.CahException(ErrorCode.CARDCAST_INVALID_ID);
        deckId = deckId.toUpperCase();

        CardcastDeck deck = cardcastService.loadSet(deckId);
        if (deck == null) throw new CahResponder.CahException(ErrorCode.CARDCAST_CANNOT_FIND);

        JsonObject obj = game.getEventJson(LongPollEvent.CARDCAST_ADD_CARDSET);
        obj.add(LongPollResponse.CARDCAST_DECK_INFO.toString(), deck.getClientMetadataJson());
        game.broadcastToPlayers(MessageType.GAME_EVENT, obj);
        game.getCardcastDeckCodes().add(deckId);

        return new JsonObject();
    }
}
