package com.gianlu.pyxreloaded.data.accounts;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GoogleAccount extends UserAccount {
    public final String subject;

    public GoogleAccount(ResultSet set) throws SQLException, BaseCahHandler.CahException {
        super(set);

        subject = set.getString("google_sub");
    }

    public GoogleAccount(String nickname, GoogleIdToken.Payload token) {
        super(nickname, token.getEmail(), Consts.AuthType.GOOGLE, (String) token.getOrDefault("picture", null));

        this.subject = token.getSubject();
    }
}
