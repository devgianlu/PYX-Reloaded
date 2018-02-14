package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.EventWrapper;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.cardcast.CardcastDeck;
import com.gianlu.pyxreloaded.cardcast.CardcastService;
import com.gianlu.pyxreloaded.data.Game;
import com.gianlu.pyxreloaded.data.GameManager;
import com.gianlu.pyxreloaded.data.QueuedMessage;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.Annotations;
import com.gianlu.pyxreloaded.servlets.BaseCahHandler;
import com.gianlu.pyxreloaded.servlets.Parameters;
import io.undertow.server.HttpServerExchange;

public class CardcastAddCardsetHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.CARDCAST_ADD_CARDSET.toString();
    private final CardcastService cardcastService;

    public CardcastAddCardsetHandler(@Annotations.GameManager GameManager gameManager, @Annotations.CardcastService CardcastService cardcastService) {
        super(gameManager);
        this.cardcastService = cardcastService;
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (game.getHost() != user) throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_GAME_HOST);
        if (game.getState() != Consts.GameState.LOBBY)
            throw new BaseCahHandler.CahException(Consts.ErrorCode.ALREADY_STARTED);

        String deckId = params.get(Consts.GeneralKeys.CARDCAST_ID);
        if (deckId == null || deckId.isEmpty()) throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);
        if (deckId.length() != 5) throw new BaseCahHandler.CahException(Consts.ErrorCode.CARDCAST_INVALID_ID);
        deckId = deckId.toUpperCase();

        CardcastDeck deck = cardcastService.loadSet(deckId);
        if (deck == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.CARDCAST_CANNOT_FIND);

        EventWrapper ev = new EventWrapper(game, Consts.Event.CARDCAST_ADD_CARDSET);
        ev.add(Consts.GeneralKeys.CARDCAST_DECK_INFO, deck.getClientMetadataJson());
        game.broadcastToPlayers(QueuedMessage.MessageType.GAME_EVENT, ev);
        game.getCardcastDeckCodes().add(deckId);

        return JsonWrapper.EMPTY;
    }
}
