package com.gianlu.pyxreloaded.game;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.singletons.Preferences;
import org.jetbrains.annotations.NotNull;

public class SuggestedGameOptions extends GameOptions {
    private final User suggester;

    public SuggestedGameOptions(Preferences preferences, @NotNull User user, String text) {
        super(preferences, text);
        suggester = user;
    }

    public User getSuggester() {
        return suggester;
    }

    public JsonWrapper toJson(String id, boolean includePassword) {
        JsonWrapper wrapper = new JsonWrapper();
        wrapper.add(Consts.GameOptionsData.OPTIONS, super.toJson(includePassword));
        wrapper.add(Consts.GameSuggestedOptionsData.ID, id);
        wrapper.add(Consts.GameSuggestedOptionsData.SUGGESTER, suggester.getNickname());
        return wrapper;
    }
}
