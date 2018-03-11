package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.cardcast.FailedLoadingSomeCardcastDecks;
import com.gianlu.pyxreloaded.cards.CardSet;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.GamesManager;
import com.gianlu.pyxreloaded.singletons.PreparingShutdown;
import io.undertow.server.HttpServerExchange;

import java.util.List;

public class StartGameHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.START_GAME.toString();

    public StartGameHandler(@Annotations.GameManager GamesManager gamesManager) {
        super(gamesManager);
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (game.getHost() != user) throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_GAME_HOST);
        if (game.getState() != Consts.GameState.LOBBY)
            throw new BaseCahHandler.CahException(Consts.ErrorCode.ALREADY_STARTED);

        PreparingShutdown.check();

        try {
            if (!game.hasEnoughCards()) {
                List<CardSet> cardSets = game.loadCardSets();
                JsonWrapper obj = new JsonWrapper();
                obj.add(Consts.GeneralGameData.BLACK_CARDS_PRESENT, game.blackCardsCount(cardSets));
                obj.add(Consts.GeneralGameData.BLACK_CARDS_REQUIRED, game.getRequiredBlackCardCount());
                obj.add(Consts.GeneralGameData.WHITE_CARDS_PRESENT, game.whiteCardsCount(cardSets));
                obj.add(Consts.GeneralGameData.WHITE_CARDS_REQUIRED, game.getRequiredWhiteCardCount());
                throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_ENOUGH_CARDS, obj);
            } else {
                game.start();
                return JsonWrapper.EMPTY;
            }
        } catch (FailedLoadingSomeCardcastDecks ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.CARDCAST_CANNOT_FIND,
                    new JsonWrapper(Consts.GeneralKeys.CARDCAST_ID, ex.getFailedJson()));
        }
    }
}
