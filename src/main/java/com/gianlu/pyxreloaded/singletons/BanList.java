package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.server.BaseCahHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class BanList {
    private final ServerDatabase db;

    public BanList(ServerDatabase db) {
        this.db = db;
    }

    public synchronized void add(String ip) throws BaseCahHandler.CahException {
        try {
            if (!db.statement().execute("INSERT INTO ban_list (ip) VALUES (" + ip + ")"))
                throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR);
        } catch (SQLException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR, ex);
        }
    }

    public synchronized boolean contains(String ip) throws BaseCahHandler.CahException {
        try (ResultSet set = db.statement().executeQuery("SELECT * FROM ban_list WHERE ip='" + ip + "'")) {
            return set.next();
        } catch (SQLException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR, ex);
        }
    }
}
