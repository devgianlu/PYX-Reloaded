package com.gianlu.pyxreloaded.data.accounts;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.twitter.TwitterProfileInfo;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TwitterAccount extends UserAccount {
    public final String id;

    public TwitterAccount(ResultSet set) throws SQLException, BaseCahHandler.CahException {
        super(set);

        id = set.getString("twitter_user_id");
    }

    public TwitterAccount(String nickname, @NotNull TwitterProfileInfo info) {
        super(nickname, info.email, Consts.AuthType.TWITTER, info.avatarUrl);

        id = info.id;
    }
}
