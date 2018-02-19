package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.cards.PyxCardSet;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.GameOptions;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.LoadedCards;
import com.gianlu.pyxreloaded.singletons.Preferences;
import com.google.gson.JsonArray;
import io.undertow.server.HttpServerExchange;

import java.util.Set;

public class FirstLoadHandler extends BaseHandler {
    public static final String OP = Consts.Operation.FIRST_LOAD.toString();
    private final LoadedCards loadedCards;
    private final Preferences preferences;

    public FirstLoadHandler(@Annotations.LoadedCards LoadedCards loadedCards, @Annotations.Preferences Preferences preferences) {
        this.loadedCards = loadedCards;
        this.preferences = preferences;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) {
        JsonWrapper obj = new JsonWrapper();

        if (user == null) {
            obj.add(Consts.GeneralKeys.IN_PROGRESS, Boolean.FALSE)
                    .add(Consts.GeneralKeys.NEXT, Consts.Operation.REGISTER.toString());
        } else {
            // They already have a session in progress, we need to figure out what they were doing
            // and tell the client where to continue from.
            obj.add(Consts.GeneralKeys.IN_PROGRESS, Boolean.TRUE)
                    .add(Consts.GeneralKeys.NICKNAME, user.getNickname());

            if (user.getGame() != null) {
                obj.add(Consts.GeneralKeys.NEXT, Consts.ReconnectNextAction.GAME.toString())
                        .add(Consts.GeneralKeys.GAME_ID, user.getGame().getId());
            } else {
                obj.add(Consts.GeneralKeys.NEXT, Consts.ReconnectNextAction.NONE.toString());
            }
        }

        Set<PyxCardSet> cardSets = loadedCards.getLoadedSets();
        JsonArray json = new JsonArray(cardSets.size());
        for (PyxCardSet cardSet : cardSets) json.add(cardSet.getClientMetadataJson().obj());
        obj.add(Consts.GameOptionsData.CARD_SETS, json)
                .add(Consts.GameOptionsData.DEFAULT_OPTIONS, GameOptions.getOptionsDefaultsJson(preferences));

        return obj;
    }
}
