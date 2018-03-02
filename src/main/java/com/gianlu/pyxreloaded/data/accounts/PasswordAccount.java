package com.gianlu.pyxreloaded.data.accounts;

import com.gianlu.pyxreloaded.Consts;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

public class PasswordAccount extends UserAccount {
    public final String hashedPassword;

    public PasswordAccount(ResultSet set) throws SQLException, ParseException {
        super(set, set.getBoolean("email_verified"));

        hashedPassword = set.getString("password");
    }

    public PasswordAccount(String username, String email, boolean emailVerified, @NotNull String hashedPassword) {
        super(username, email, Consts.AuthType.PASSWORD, emailVerified, null); // TODO: Avatar

        this.hashedPassword = hashedPassword;
    }
}
