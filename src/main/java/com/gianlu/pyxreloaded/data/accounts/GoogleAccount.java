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

    public GoogleAccount(String username, String email, GoogleIdToken.Payload payload) {
        super(username, email, Consts.AuthType.GOOGLE);

        this.subject = payload.getSubject();
    }
}
