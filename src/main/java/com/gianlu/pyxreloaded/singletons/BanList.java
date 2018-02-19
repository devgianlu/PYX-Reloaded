package com.gianlu.pyxreloaded.singletons;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class BanList { // TODO: Should be saved on disk
    private static BanList instance;
    private final List<String> list;

    private BanList() {
        list = new ArrayList<>();
    }

    @NotNull
    public static BanList get() {
        if (instance == null) instance = new BanList();
        return instance;
    }

    public synchronized void add(String ban) {
        list.add(ban);
    }

    public synchronized boolean contains(String ban) {
        return list.contains(ban);
    }
}
