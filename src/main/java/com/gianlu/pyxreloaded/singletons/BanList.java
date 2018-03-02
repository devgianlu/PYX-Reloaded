package com.gianlu.pyxreloaded.singletons;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class BanList {
    private final ServerDatabase db;

    public BanList(ServerDatabase db) {
        this.db = db;
    }

    public synchronized void add(String ip) {
        try {
            if (!db.statement().execute("INSERT INTO ban_list (ip) VALUES ('" + ip + "')"))
                throw new IllegalStateException();
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
