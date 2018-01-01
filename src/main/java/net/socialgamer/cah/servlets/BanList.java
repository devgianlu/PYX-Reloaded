package net.socialgamer.cah.servlets;

import java.util.ArrayList;
import java.util.List;

public final class BanList {
    private static final List<String> banList = new ArrayList<>();

    public static void add(String ban) {
        banList.add(ban);
    }

    public static boolean contains(String ban) {
        return banList.contains(ban);
    }
}
