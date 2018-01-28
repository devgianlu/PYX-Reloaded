package net.socialgamer.cah.servlets;

import net.socialgamer.cah.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public final class AdminToken {
    public static final int TOKEN_LENGTH = 36;
    private static final AtomicReference<String> token = new AtomicReference<>(null);

    public static String refresh() {
        String newToken = Utils.generateAlphanumericString(TOKEN_LENGTH);
        synchronized (token) {
            token.set(newToken);
        }

        return newToken;
    }

    @NotNull
    public static String current() {
        synchronized (token) {
            return token.get();
        }
    }
}
