package net.socialgamer.cah.handlers;

import com.google.gson.JsonArray;
import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.Constants.ReconnectNextAction;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.db.LoadedCards;
import net.socialgamer.cah.db.PyxCardSet;
import net.socialgamer.cah.servlets.Parameters;

import java.util.Set;

public class FirstLoadHandler extends BaseHandler {
    public static final String OP = AjaxOperation.FIRST_LOAD.toString();

    public FirstLoadHandler() {
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) {
        JsonWrapper obj = new JsonWrapper();

        if (user == null) {
            obj.add(AjaxResponse.IN_PROGRESS, Boolean.FALSE)
                    .add(AjaxResponse.NEXT, AjaxOperation.REGISTER.toString());
        } else {
            // They already have a session in progress, we need to figure out what they were doing
            // and tell the client where to continue from.
            obj.add(AjaxResponse.IN_PROGRESS, Boolean.TRUE)
                    .add(AjaxResponse.NICKNAME, user.getNickname());

            if (user.getGame() != null) {
                obj.add(AjaxResponse.NEXT, ReconnectNextAction.GAME.toString())
                        .add(AjaxResponse.GAME_ID, user.getGame().getId());
            } else {
                obj.add(AjaxResponse.NEXT, ReconnectNextAction.NONE.toString());
            }
        }

        Set<PyxCardSet> cardSets = LoadedCards.getLoadedSets();
        JsonArray json = new JsonArray(cardSets.size());
        for (PyxCardSet cardSet : cardSets) json.add(cardSet.getClientMetadataJson());
        obj.add(AjaxResponse.CARD_SETS, json);

        return obj;
    }
}
