package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.accounts.*;
import com.gianlu.pyxreloaded.facebook.FacebookProfileInfo;
import com.gianlu.pyxreloaded.facebook.FacebookToken;
import com.gianlu.pyxreloaded.github.GithubProfileInfo;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.twitter.TwitterProfileInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class UsersWithAccount {
    private final ServerDatabase db;

    public UsersWithAccount(ServerDatabase db) {
        this.db = db;
    }

    @NotNull
    private static String VALUES(String... values) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");

        boolean first = true;
        for (@Nullable String val : values) {
            if (!first) builder.append(",");
            if (val == null) builder.append("NULL");
            else builder.append("'").append(val).append("'");
            first = false;
        }

        return builder.append(")").toString();
    }

    @Nullable
    public PasswordAccount getPasswordAccount(@NotNull String nickname) throws BaseCahHandler.CahException {
        try (ResultSet set = db.statement().executeQuery("SELECT * FROM users WHERE username='" + nickname + "'")) {
            return set.next() ? new PasswordAccount(set) : null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Nullable
    public GoogleAccount getGoogleAccount(@NotNull GoogleIdToken.Payload token) throws BaseCahHandler.CahException {
        try (ResultSet set = db.statement().executeQuery("SELECT * FROM users WHERE google_sub='" + token.getSubject() + "'")) {
            return set.next() ? new GoogleAccount(set, token) : null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Nullable
    public FacebookAccount getFacebookAccount(@NotNull FacebookToken token) throws BaseCahHandler.CahException {
        try (ResultSet set = db.statement().executeQuery("SELECT * FROM users WHERE facebook_user_id='" + token.userId + "'")) {
            return set.next() ? new FacebookAccount(set) : null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Nullable
    public GithubAccount getGithubAccount(@NotNull GithubProfileInfo info) throws BaseCahHandler.CahException {
        try (ResultSet set = db.statement().executeQuery("SELECT * FROM users WHERE github_user_id='" + info.id + "'")) {
            return set.next() ? new GithubAccount(set, info) : null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Nullable
    public TwitterAccount getTwitterAccount(@NotNull TwitterProfileInfo info) throws BaseCahHandler.CahException {
        try (ResultSet set = db.statement().executeQuery("SELECT * FROM users WHERE twitter_user_id='" + info.id + "'")) {
            return set.next() ? new TwitterAccount(set) : null;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean hasEmail(@NotNull String email) {
        try (ResultSet set = db.statement().executeQuery("SELECT count(*) FROM users WHERE email='" + email + "'")) {
            return set.getInt(1) > 0;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean hasNickname(@NotNull String nickname) {
        try (ResultSet set = db.statement().executeQuery("SELECT count(*) FROM users WHERE username='" + nickname + "'")) {
            return set.getInt(1) > 0;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void addAccount(@NotNull PasswordAccount account) {
        try (Statement statement = db.statement()) {
            int result = statement.executeUpdate("INSERT INTO users (username, auth, email, avatar_url, password) VALUES "
                    + VALUES(account.username, Consts.AuthType.PASSWORD.toString(), account.email, account.avatarUrl, account.hashedPassword));

            if (result != 1) throw new IllegalStateException();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void addAccount(@NotNull GoogleAccount account) {
        try (Statement statement = db.statement()) {
            int result = statement.executeUpdate("INSERT INTO users (username, auth, email, avatar_url, google_sub) VALUES " +
                    VALUES(account.username, Consts.AuthType.GOOGLE.toString(), account.email, account.avatarUrl, account.subject));

            if (result != 1) throw new IllegalStateException();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void addAccount(@NotNull FacebookAccount account) {
        try (Statement statement = db.statement()) {
            int result = statement.executeUpdate("INSERT INTO users (username, auth, email, avatar_url, facebook_user_id) VALUES " +
                    VALUES(account.username, Consts.AuthType.FACEBOOK.toString(), account.email, account.avatarUrl, account.userId));

            if (result != 1) throw new IllegalStateException();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void addAccount(@NotNull GithubAccount account) {
        try (Statement statement = db.statement()) {
            int result = statement.executeUpdate("INSERT INTO users (username, auth, email, avatar_url, github_user_id) VALUES " +
                    VALUES(account.username, Consts.AuthType.GITHUB.toString(), account.email, account.avatarUrl, account.id));

            if (result != 1) throw new IllegalStateException();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void addAccount(@NotNull TwitterAccount account) {
        try (Statement statement = db.statement()) {
            int result = statement.executeUpdate("INSERT INTO users (username, auth, email, avatar_url, twitter_user_id) VALUES " +
                    VALUES(account.username, Consts.AuthType.TWITTER.toString(), account.email, account.avatarUrl, account.id));

            if (result != 1) throw new IllegalStateException();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @NotNull
    public PasswordAccount registerWithPassword(@NotNull String nickname, @NotNull String email, @NotNull String password) {
        PasswordAccount account = new PasswordAccount(nickname, email, BCrypt.hashpw(password, BCrypt.gensalt()));
        addAccount(account);
        return account;
    }

    @NotNull
    public GoogleAccount registerWithGoogle(@NotNull String nickname, @NotNull GoogleIdToken.Payload token) {
        GoogleAccount account = new GoogleAccount(nickname, token);
        addAccount(account);
        return account;
    }

    public FacebookAccount registerWithFacebook(@NotNull String nickname, @NotNull FacebookToken token, @NotNull FacebookProfileInfo info) {
        FacebookAccount account = new FacebookAccount(nickname, token, info);
        addAccount(account);
        return account;
    }

    public GithubAccount registerWithGithub(@NotNull String nickname, @NotNull GithubProfileInfo info) {
        GithubAccount account = new GithubAccount(nickname, info);
        addAccount(account);
        return account;
    }

    public TwitterAccount registerWithTwitter(@NotNull String nickname, @NotNull TwitterProfileInfo info) {
        TwitterAccount account = new TwitterAccount(nickname, info);
        addAccount(account);
        return account;
    }
}
