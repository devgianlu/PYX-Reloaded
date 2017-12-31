package net.socialgamer.cah.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.Constants.ReconnectNextAction;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.db.PyxCardSet;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.Parameters;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class FirstLoadHandler extends BaseHandler {
    public static final String OP = AjaxOperation.FIRST_LOAD.toString();
    private final Session hibernateSession;
    private final Boolean includeInactiveCardsets;

    public FirstLoadHandler(@Annotations.HibernateSession Session hibernateSession, @Annotations.IncludeInactiveCardsets boolean includeInactiveCardsets) {
        this.hibernateSession = hibernateSession;
        this.includeInactiveCardsets = includeInactiveCardsets;
    }

    @Override
    public JsonElement handle(User user, Parameters params, NanoHTTPD.IHTTPSession session) {
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

        // FIXME: Cards shouldn't be loaded every time

        try {
            Transaction transaction = hibernateSession.beginTransaction();
            List<PyxCardSet> cardSets = hibernateSession
                    .createQuery(PyxCardSet.getCardsetQuery(includeInactiveCardsets))
                    .setReadOnly(true)
                    .setCacheable(true)
                    .list();

            JsonArray json = new JsonArray(cardSets.size());
            for (PyxCardSet cardSet : cardSets) json.add(cardSet.getClientMetadataJson(hibernateSession));
            obj.add(AjaxResponse.CARD_SETS.toString(), json);
            transaction.commit();
        } finally {
            hibernateSession.close();
        }

        return obj;
    }
}
