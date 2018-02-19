package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Utils;
import com.gianlu.pyxreloaded.data.User;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class Sessions {
    private static final int SID_LENGTH = 24;
    private static Sessions instance;
    private final Map<String, User> sessions;

    private Sessions() {
        sessions = new HashMap<>();
    }

    @NotNull
    public static Sessions get() {
        if (instance == null) instance = new Sessions();
        return instance;
    }

    @NotNull
    public static String generateNewId() {
        return Utils.generateAlphanumericString(SID_LENGTH);
    }

    @Contract("null -> null")
    @Nullable
    public synchronized User getUser(String sid) {
        if (sid == null || sid.isEmpty() || sid.length() != SID_LENGTH) return null;
        return sessions.get(sid);
    }

    public synchronized void invalidate(String sid) {
        sessions.remove(sid);
    }

    @NotNull
    public synchronized String add(@NotNull User user) {
        sessions.put(user.getSessionId(), user);
        return user.getSessionId();
    }
}
