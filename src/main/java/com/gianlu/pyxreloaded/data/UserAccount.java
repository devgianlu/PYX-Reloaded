package com.gianlu.pyxreloaded.data;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.server.BaseCahHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserAccount {
    public final String nickname;
    public final String email;
    public final Consts.AuthType auth;
    public final String password;

    public UserAccount(ResultSet set) throws SQLException, BaseCahHandler.CahException {
        nickname = set.getString("username");
        email = set.getString("email");
        auth = Consts.AuthType.parse(set.getString("auth"));

        password = set.getString("password");
    }
}
