package net.socialgamer.cah.servlets;

import net.socialgamer.cah.data.User;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public final class Sessions {
    private final static Map<String, User> sessions = new HashMap<>();

    @Contract("null -> null")
    @Nullable
    public static User getUser(String sid) {
        if (sid == null || sid.isEmpty() || sid.length() != 12) return null;
        return sessions.get(sid);
    }

    public static void invalidate(String sid) {
        sessions.remove(sid);
    }
}
