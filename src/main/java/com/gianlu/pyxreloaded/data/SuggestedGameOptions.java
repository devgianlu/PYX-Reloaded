package com.gianlu.pyxreloaded.data;

import com.gianlu.pyxreloaded.Preferences;
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
}
