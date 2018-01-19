package net.socialgamer.cah.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.Constants.ReconnectNextAction;
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
    public JsonElement handle(User user, Parameters params, HttpServerExchange exchange) {
        JsonObject obj = new JsonObject();

        if (user == null) {
            obj.addProperty(AjaxResponse.IN_PROGRESS.toString(), Boolean.FALSE);
            obj.addProperty(AjaxResponse.NEXT.toString(), AjaxOperation.REGISTER.toString());
        } else {
            // They already have a session in progress, we need to figure out what they were doing
            // and tell the client where to continue from.
            obj.addProperty(AjaxResponse.IN_PROGRESS.toString(), Boolean.TRUE);
            obj.addProperty(AjaxResponse.NICKNAME.toString(), user.getNickname());

            if (user.getGame() != null) {
                obj.addProperty(AjaxResponse.NEXT.toString(), ReconnectNextAction.GAME.toString());
                obj.addProperty(AjaxResponse.GAME_ID.toString(), user.getGame().getId());
            } else {
                obj.addProperty(AjaxResponse.NEXT.toString(), ReconnectNextAction.NONE.toString());
            }
        }

        Set<PyxCardSet> cardSets = LoadedCards.getLoadedSets();
        JsonArray json = new JsonArray(cardSets.size());
        for (PyxCardSet cardSet : cardSets) json.add(cardSet.getClientMetadataJson());
        obj.add(AjaxResponse.CARD_SETS.toString(), json);

        return obj;
    }
}
