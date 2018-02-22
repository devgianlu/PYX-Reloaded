package com.gianlu.pyxreloaded.data;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserAccount {
    public final String username;
    public final String email;
    public final Consts.AuthType auth;
    public final String hashedPassword;

    public UserAccount(ResultSet set) throws SQLException, BaseCahHandler.CahException {
        username = set.getString("username");
        email = set.getString("email");
        auth = Consts.AuthType.parse(set.getString("auth"));

        hashedPassword = set.getString("password");
    }

    public UserAccount(String username, String email, Consts.AuthType auth, @Nullable String hashedPassword) {
        this.username = username;
        this.email = email;
        this.auth = auth;
        this.hashedPassword = hashedPassword;
    }

    public JsonWrapper toJson() {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.GeneralKeys.EMAIL, email);
        obj.add(Consts.GeneralKeys.NICKNAME, username);
        return obj;
    }
}
