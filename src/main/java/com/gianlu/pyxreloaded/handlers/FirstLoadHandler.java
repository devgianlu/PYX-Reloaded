package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.cards.PyxCardSet;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.GameOptions;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.Emails;
import com.gianlu.pyxreloaded.singletons.LoadedCards;
import com.gianlu.pyxreloaded.singletons.Preferences;
import com.gianlu.pyxreloaded.singletons.SocialLogin;
import com.google.gson.JsonArray;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class FirstLoadHandler extends BaseHandler {
    public static final String OP = Consts.Operation.FIRST_LOAD.toString();
    private final JsonWrapper defaultGameOptions;
    private final JsonArray cards;
    private final JsonWrapper authConfig;
    private final String serverStatusPage;

    public FirstLoadHandler(@Annotations.LoadedCards LoadedCards loadedCards,
                            @Annotations.Emails Emails emails,
                            @Annotations.SocialLogin SocialLogin socials,
                            @Annotations.Preferences Preferences preferences) {
        serverStatusPage = preferences.getStringNotEmpty("serverStatusPage", null);

        Set<PyxCardSet> cardSets = loadedCards.getLoadedSets();
        cards = new JsonArray(cardSets.size());
        for (PyxCardSet cardSet : cardSets) cards.add(cardSet.getClientMetadataJson().obj());

        defaultGameOptions = GameOptions.getOptionsDefaultsJson(preferences);

        authConfig = new JsonWrapper();
        if (emails.enabled()) authConfig.add(Consts.AuthType.PASSWORD, emails.senderEmail());
        if (socials.googleEnabled()) authConfig.add(Consts.AuthType.GOOGLE, socials.googleAppId());
        if (socials.facebookEnabled()) authConfig.add(Consts.AuthType.FACEBOOK, socials.facebookAppId());
        if (socials.githubEnabled()) authConfig.add(Consts.AuthType.GITHUB, socials.githubAppId());
        if (socials.twitterEnabled()) authConfig.add(Consts.AuthType.TWITTER, socials.twitterAppId());
    }

    @NotNull
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
                    .add(Consts.UserData.NICKNAME, user.getNickname());

            if (user.getGame() != null) {
                obj.add(Consts.GeneralKeys.NEXT, Consts.ReconnectNextAction.GAME.toString())
                        .add(Consts.GeneralKeys.GAME_ID, user.getGame().getId());
            } else {
                obj.add(Consts.GeneralKeys.NEXT, Consts.ReconnectNextAction.NONE.toString());
            }
        }

        obj.add(Consts.GeneralKeys.AUTH_CONFIG, authConfig);
        obj.add(Consts.GameOptionsData.CARD_SETS, cards);
        obj.add(Consts.GameOptionsData.DEFAULT_OPTIONS, defaultGameOptions);
        obj.add(Consts.GeneralKeys.SERVER_STATUS_PAGE, serverStatusPage);

        return obj;
    }
}
