package com.gianlu.pyxreloaded.data.accounts;

import com.gianlu.pyxreloaded.Consts;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

public class GoogleAccount extends UserAccount {
    public final String subject;

    public GoogleAccount(ResultSet set, GoogleIdToken.Payload token) throws SQLException, ParseException {
        super(set, token.getEmailVerified());

        subject = set.getString("google_sub");
    }

    public GoogleAccount(String nickname, GoogleIdToken.Payload token) {
        super(nickname, token.getEmail(), Consts.AuthType.GOOGLE, token.getEmailVerified(), (String) token.getOrDefault("picture", null));

        this.subject = token.getSubject();
    }
}
