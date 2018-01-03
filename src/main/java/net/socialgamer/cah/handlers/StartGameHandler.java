package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.Constants.ErrorInformation;
import net.socialgamer.cah.Constants.GameState;
import net.socialgamer.cah.Utils;
import net.socialgamer.cah.cardcast.FailedLoadingSomeCardcastDecks;
import net.socialgamer.cah.data.CardSet;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;

import java.util.List;

public class StartGameHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.START_GAME.toString();

    public StartGameHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonElement handleWithUserInGame(User user, Game game, Parameters params, NanoHTTPD.IHTTPSession session) throws CahResponder.CahException {
        if (game.getHost() != user) throw new CahResponder.CahException(ErrorCode.NOT_GAME_HOST);
        if (game.getState() != GameState.LOBBY) throw new CahResponder.CahException(ErrorCode.ALREADY_STARTED);

        try {
            if (!game.hasEnoughCards()) {
                List<CardSet> cardSets = game.loadCardSets();
                JsonObject obj = new JsonObject();
                obj.addProperty(ErrorInformation.BLACK_CARDS_PRESENT.toString(), game.blackCardsCount(cardSets));
                obj.addProperty(ErrorInformation.BLACK_CARDS_REQUIRED.toString(), game.getRequiredBlackCardCount());
                obj.addProperty(ErrorInformation.WHITE_CARDS_PRESENT.toString(), game.whiteCardsCount(cardSets));
                obj.addProperty(ErrorInformation.WHITE_CARDS_REQUIRED.toString(), game.getRequiredWhiteCardCount());
                throw new CahResponder.CahException(ErrorCode.NOT_ENOUGH_CARDS, obj);
            } else {
                game.start();
                return new JsonObject();
            }
        } catch (FailedLoadingSomeCardcastDecks ex) {
            throw new CahResponder.CahException(ErrorCode.CARDCAST_CANNOT_FIND,
                    Utils.singletonJsonObject(Constants.AjaxResponse.CARDCAST_ID.toString(),
                            ex.getFailedJson()));
        }
    }
}
