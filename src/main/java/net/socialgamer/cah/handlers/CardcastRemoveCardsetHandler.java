package net.socialgamer.cah.handlers;

import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants.*;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.cardcast.CardcastDeck;
import net.socialgamer.cah.cardcast.CardcastService;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.QueuedMessage.MessageType;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.BaseCahHandler;
import net.socialgamer.cah.servlets.Parameters;

public class CardcastRemoveCardsetHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.CARDCAST_REMOVE_CARDSET.toString();
    private final CardcastService cardcastService;

    public CardcastRemoveCardsetHandler(@Annotations.GameManager GameManager gameManager, @Annotations.CardcastService CardcastService cardcastService) {
        super(gameManager);
        this.cardcastService = cardcastService;
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (game.getHost() != user) throw new BaseCahHandler.CahException(ErrorCode.NOT_GAME_HOST);
        if (game.getState() != GameState.LOBBY) throw new BaseCahHandler.CahException(ErrorCode.ALREADY_STARTED);

        String deckId = params.get(AjaxRequest.CARDCAST_ID);
        if (deckId == null || deckId.isEmpty()) throw new BaseCahHandler.CahException(ErrorCode.BAD_REQUEST);
        if (deckId.length() != 5) throw new BaseCahHandler.CahException(ErrorCode.CARDCAST_INVALID_ID);
        deckId = deckId.toUpperCase();

        // Remove it from the set regardless if it loads or not.
        game.getCardcastDeckIds().remove(deckId);
        final CardcastDeck deck = cardcastService.loadSet(deckId);
        if (deck == null) throw new BaseCahHandler.CahException(ErrorCode.CARDCAST_CANNOT_FIND);

        JsonObject obj = game.getEventJson(LongPollEvent.CARDCAST_REMOVE_CARDSET);
        obj.add(LongPollResponse.CARDCAST_DECK_INFO.toString(), deck.getClientMetadataJson());
        game.broadcastToPlayers(MessageType.GAME_EVENT, obj);

        return JsonWrapper.EMPTY;
    }
}
