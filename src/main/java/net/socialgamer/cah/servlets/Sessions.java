package net.socialgamer.cah.servlets;

import net.socialgamer.cah.data.User;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class Sessions {
    private final static Map<String, User> sessions = new HashMap<>();
    private static final int SID_LENGTH = 12;
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Contract("null -> null")
    @Nullable
    public static User getUser(String sid) {
        if (sid == null || sid.isEmpty() || sid.length() != SID_LENGTH) return null;
        return sessions.get(sid);
    }

    @NotNull
    public static String generateNewId() {
        StringBuilder builder = new StringBuilder();
        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < SID_LENGTH; i++) {
            if (random.nextBoolean()) builder.append(String.valueOf(random.nextInt(10)));
            else builder.append(ALPHABET.charAt(random.nextInt(26)));
        }

        return builder.toString();
    }

    public static void invalidate(String sid) {
        sessions.remove(sid);
    }

    public static void add(User user) {
        sessions.put(user.getSessionId(), user);
    }
}
