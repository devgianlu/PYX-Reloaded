package com.gianlu.pyxreloaded.data.accounts;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PasswordAccount extends UserAccount {
    public final String hashedPassword;

    public PasswordAccount(ResultSet set) throws SQLException, BaseCahHandler.CahException {
        super(set, false); // TODO

        hashedPassword = set.getString("password");
    }

    public PasswordAccount(String username, String email, @NotNull String hashedPassword) {
        super(username, email, Consts.AuthType.PASSWORD, false, null); // TODO: Avatar and email verified

        this.hashedPassword = hashedPassword;
    }
}
