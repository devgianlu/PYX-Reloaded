package com.gianlu.pyxreloaded.data.accounts;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.facebook.FacebookProfileInfo;
import com.gianlu.pyxreloaded.facebook.FacebookToken;
import com.gianlu.pyxreloaded.server.BaseCahHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FacebookAccount extends UserAccount {
    public final String userId;

    public FacebookAccount(ResultSet set) throws BaseCahHandler.CahException, SQLException {
        super(set);

        userId = set.getString("facebook_user_id");
    }

    public FacebookAccount(String nickname, FacebookToken token, FacebookProfileInfo info) {
        super(nickname, info.email, Consts.AuthType.FACEBOOK, info.pictureUrl);

        userId = token.userId;
    }
}
