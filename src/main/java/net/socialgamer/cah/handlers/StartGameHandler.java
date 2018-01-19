package net.socialgamer.cah.handlers;

import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.Constants.ErrorInformation;
import net.socialgamer.cah.Constants.GameState;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.Utils;
import net.socialgamer.cah.cardcast.FailedLoadingSomeCardcastDecks;
import net.socialgamer.cah.data.CardSet;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.BaseCahHandler;
import net.socialgamer.cah.servlets.Parameters;

import java.util.List;

public class StartGameHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.START_GAME.toString();

    public StartGameHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (game.getHost() != user) throw new BaseCahHandler.CahException(ErrorCode.NOT_GAME_HOST);
        if (game.getState() != GameState.LOBBY) throw new BaseCahHandler.CahException(ErrorCode.ALREADY_STARTED);

        try {
            if (!game.hasEnoughCards()) {
                List<CardSet> cardSets = game.loadCardSets();
                JsonObject obj = new JsonObject();
                obj.addProperty(ErrorInformation.BLACK_CARDS_PRESENT.toString(), game.blackCardsCount(cardSets));
                obj.addProperty(ErrorInformation.BLACK_CARDS_REQUIRED.toString(), game.getRequiredBlackCardCount());
                obj.addProperty(ErrorInformation.WHITE_CARDS_PRESENT.toString(), game.whiteCardsCount(cardSets));
                obj.addProperty(ErrorInformation.WHITE_CARDS_REQUIRED.toString(), game.getRequiredWhiteCardCount());
                throw new BaseCahHandler.CahException(ErrorCode.NOT_ENOUGH_CARDS, obj);
            } else {
                ErrorCode error = game.start();
                if (error != null) throw new BaseCahHandler.CahException(error);
                else return JsonWrapper.EMPTY;
            }
        } catch (FailedLoadingSomeCardcastDecks ex) {
            throw new BaseCahHandler.CahException(ErrorCode.CARDCAST_CANNOT_FIND,
                    Utils.singletonJsonObject(Constants.AjaxResponse.CARDCAST_ID.toString(),
                            ex.getFailedJson()));
        }
    }
}
