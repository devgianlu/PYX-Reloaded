package com.gianlu.pyxreloaded.data.accounts;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.socials.twitter.TwitterProfileInfo;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;

public class TwitterAccount extends UserAccount {
    public final String id;

    public TwitterAccount(ResultSet set) throws SQLException, ParseException {
        super(set, true); // Cannot even register without a verified email

        id = set.getString("twitter_user_id");
    }

    public TwitterAccount(String nickname, @NotNull TwitterProfileInfo info) {
        super(nickname, info.email, Consts.AuthType.TWITTER, true, info.avatarUrl);

        id = info.id;
    }
}
