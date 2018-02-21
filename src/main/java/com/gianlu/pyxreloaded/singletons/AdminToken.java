package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Utils;
import org.jetbrains.annotations.NotNull;

public final class AdminToken {
    public static final int TOKEN_LENGTH = 36;
    private static AdminToken instance;
    private String token;

    private AdminToken() {
        token = refresh();
    }

    @NotNull
    public static AdminToken get() {
        if (instance == null) instance = new AdminToken();
        return instance;
    }

    @NotNull
    public synchronized String refresh() {
        token = Utils.generateAlphanumericString(TOKEN_LENGTH);
        return token;
    }

    @NotNull
    public synchronized String current() {
        return token;
    }
}
