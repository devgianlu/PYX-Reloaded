package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.handlers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Handlers {
    public final static Map<String, Class<? extends BaseHandler>> LIST;
    private final static List<String> SKIP_USER_CHECK;

    static {
        LIST = new HashMap<>();
        LIST.put(BanHandler.OP, BanHandler.class);
        LIST.put(CardcastAddCardsetHandler.OP, CardcastAddCardsetHandler.class);
        LIST.put(CardcastListCardsetsHandler.OP, CardcastListCardsetsHandler.class);
        LIST.put(CardcastRemoveCardsetHandler.OP, CardcastRemoveCardsetHandler.class);
        LIST.put(ChangeGameOptionsHandler.OP, ChangeGameOptionsHandler.class);
        LIST.put(ChatHandler.OP, ChatHandler.class);
        LIST.put(CreateGameHandler.OP, CreateGameHandler.class);
        LIST.put(FirstLoadHandler.OP, FirstLoadHandler.class);
        LIST.put(GameChatHandler.OP, GameChatHandler.class);
        LIST.put(GameListHandler.OP, GameListHandler.class);
        LIST.put(GetCardsHandler.OP, GetCardsHandler.class);
        LIST.put(GetGameInfoHandler.OP, GetGameInfoHandler.class);
        LIST.put(JoinGameHandler.OP, JoinGameHandler.class);
        LIST.put(JudgeSelectHandler.OP, JudgeSelectHandler.class);
        LIST.put(KickHandler.OP, KickHandler.class);
        LIST.put(LeaveGameHandler.OP, LeaveGameHandler.class);
        LIST.put(LogoutHandler.OP, LogoutHandler.class);
        LIST.put(NamesHandler.OP, NamesHandler.class);
        LIST.put(PlayCardHandler.OP, PlayCardHandler.class);
        LIST.put(RegisterHandler.OP, RegisterHandler.class);
        LIST.put(SpectateGameHandler.OP, SpectateGameHandler.class);
        LIST.put(StartGameHandler.OP, StartGameHandler.class);
        LIST.put(StopGameHandler.OP, StopGameHandler.class);
        LIST.put(LikeGameHandler.OP, LikeGameHandler.class);
        LIST.put(DislikeGameHandler.OP, DislikeGameHandler.class);
        LIST.put(GetMeHandler.OP, GetMeHandler.class);
        LIST.put(PongHandler.OP, PongHandler.class);
        LIST.put(GameOptionsSuggestionDecisionHandler.OP, GameOptionsSuggestionDecisionHandler.class);
        LIST.put(GetSuggestedGameOptionsHandler.OP, GetSuggestedGameOptionsHandler.class);
        LIST.put(CreateAccountHandler.OP, CreateAccountHandler.class);
        LIST.put(PrepareShutdownHandler.OP, PrepareShutdownHandler.class);
        LIST.put(GetUserPreferencesHandler.OP, GetUserPreferencesHandler.class);
        LIST.put(SetUserPreferencesHandler.OP, SetUserPreferencesHandler.class);

        SKIP_USER_CHECK = new ArrayList<>();
        SKIP_USER_CHECK.add(RegisterHandler.OP);
        SKIP_USER_CHECK.add(FirstLoadHandler.OP);
        SKIP_USER_CHECK.add(CreateAccountHandler.OP);
    }

    public static boolean skipUserCheck(String op) {
        return SKIP_USER_CHECK.contains(op);
    }
}
