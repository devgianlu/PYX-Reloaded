package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.UserAccount;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class UsersWithAccount {
    private final ServerDatabase db;

    public UsersWithAccount(ServerDatabase db) {
        this.db = db;
    }

    @Nullable
    public UserAccount getAccount(String nickname) throws BaseCahHandler.CahException {
        try (ResultSet set = db.statement().executeQuery("SELECT * FROM users WHERE username='" + nickname + "'")) {
            if (!set.next()) return null;
            return new UserAccount(set);
        } catch (SQLException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR, ex);
        }
    }
}
