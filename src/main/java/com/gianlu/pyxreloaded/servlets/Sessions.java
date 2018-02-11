package com.gianlu.pyxreloaded.servlets;

import com.gianlu.pyxreloaded.Utils;
import com.gianlu.pyxreloaded.data.User;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class Sessions {
    private final static Map<String, User> sessions = new HashMap<>();
    private static final int SID_LENGTH = 24;

    @Contract("null -> null")
    @Nullable
    public static User getUser(String sid) {
        if (sid == null || sid.isEmpty() || sid.length() != SID_LENGTH) return null;
        return sessions.get(sid);
    }

    @NotNull
    public static String generateNewId() {
        return Utils.generateAlphanumericString(SID_LENGTH);
    }

    public static void invalidate(String sid) {
        sessions.remove(sid);
    }

    public static String add(User user) {
        sessions.put(user.getSessionId(), user);
        return user.getSessionId();
    }
}
