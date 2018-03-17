package com.gianlu.pyxreloaded.data.accounts;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.socials.facebook.FacebookProfileInfo;
import com.gianlu.pyxreloaded.socials.facebook.FacebookToken;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

public class FacebookAccount extends UserAccount {
    public final String userId;

    public FacebookAccount(ResultSet user) throws SQLException, ParseException {
        super(user, true); // Cannot even register without a verified email

        userId = user.getString("facebook_user_id");
    }

    public FacebookAccount(String nickname, FacebookToken token, FacebookProfileInfo info) {
        super(nickname, info.email, Consts.AuthType.FACEBOOK, true, info.pictureUrl);

        userId = token.userId;
    }
}
