package com.gianlu.pyxreloaded.singletons;

import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class BanList {
    private final ServerDatabase db;

    public BanList(ServerDatabase db) {
        this.db = db;
    }

    public synchronized void add(@NotNull String ip) {
        try {
            db.statement().execute("INSERT OR IGNORE INTO ban_list (ip) VALUES ('" + ip + "')");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public synchronized boolean contains(String ip) {
        try (ResultSet set = db.statement().executeQuery("SELECT * FROM ban_list WHERE ip='" + ip + "'")) {
            return set.next();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
