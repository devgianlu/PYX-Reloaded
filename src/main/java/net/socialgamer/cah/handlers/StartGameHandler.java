package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.Constants.ErrorInformation;
import net.socialgamer.cah.Constants.GameState;
import net.socialgamer.cah.data.CardSet;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;
import org.hibernate.Session;
import org.hibernate.exception.JDBCConnectionException;

import java.util.List;

public class StartGameHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.START_GAME.toString();
    private final Session hibernateSession;

    public StartGameHandler(final GameManager gameManager, final Session session) {
        super(gameManager);
        this.hibernateSession = session;
    }

    @Override
    public JsonElement handleWithUserInGame(User user, Game game, Parameters params, NanoHTTPD.IHTTPSession session) throws CahResponder.CahException {
        try {
            if (game.getHost() != user) throw new CahResponder.CahException(ErrorCode.NOT_GAME_HOST);
            if (game.getState() != GameState.LOBBY) throw new CahResponder.CahException(ErrorCode.ALREADY_STARTED);

            if (!game.hasEnoughCards(hibernateSession)) {
                final List<CardSet> cardSets = game.loadCardSets(hibernateSession);
                JsonObject obj = new JsonObject();
                obj.addProperty(ErrorInformation.BLACK_CARDS_PRESENT.toString(), game.loadBlackDeck(cardSets).totalCount());
                obj.addProperty(ErrorInformation.BLACK_CARDS_REQUIRED.toString(), Game.MINIMUM_BLACK_CARDS);
                obj.addProperty(ErrorInformation.WHITE_CARDS_PRESENT.toString(), game.loadWhiteDeck(cardSets).totalCount());
                obj.addProperty(ErrorInformation.WHITE_CARDS_REQUIRED.toString(), game.getRequiredWhiteCardCount());
                throw new CahResponder.CahException(ErrorCode.NOT_ENOUGH_CARDS, obj);
            } else if (!game.start()) {
                throw new CahResponder.CahException(ErrorCode.NOT_ENOUGH_PLAYERS);
            } else {
                return new JsonObject();
            }
        } catch (final JDBCConnectionException ex) {
            throw new CahResponder.CahException(ErrorCode.SERVER_ERROR, ex);
        } finally {
            hibernateSession.close();
        }
    }
}
