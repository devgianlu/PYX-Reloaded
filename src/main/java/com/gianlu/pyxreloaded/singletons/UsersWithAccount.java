package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.UserAccount;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class UsersWithAccount {
    private final ServerDatabase db;

    public UsersWithAccount(ServerDatabase db) {
        this.db = db;
    }

    @Nullable
    public UserAccount getAccountByNickname(String nickname) throws BaseCahHandler.CahException {
        try (ResultSet set = getAccountSetByNickname(nickname)) {
            return set == null ? null : new UserAccount(set);
        } catch (SQLException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR, ex);
        }
    }

    @Nullable
    public UserAccount getGoogleAccount(String email) throws BaseCahHandler.CahException {
        try (ResultSet set = getAccountSetByGoogleSubject(email)) {
            return set == null ? null : new UserAccount(set);
        } catch (SQLException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR, ex);
        }
    }

    @Nullable
    private ResultSet getAccountSetByGoogleSubject(String sub) throws BaseCahHandler.CahException {
        try {
            ResultSet set = db.statement().executeQuery("SELECT * FROM users WHERE google_sub='" + sub + "'");
            if (!set.next()) return null;
            return set; // Doesn't close the result set
        } catch (SQLException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR, ex);
        }
    }

    @Nullable
    private ResultSet getAccountSetByNickname(String nickname) throws BaseCahHandler.CahException {
        try {
            ResultSet set = db.statement().executeQuery("SELECT * FROM users WHERE username='" + nickname + "'");
            if (!set.next()) return null;
            return set; // Doesn't close the result set
        } catch (SQLException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR, ex);
        }
    }

    public boolean hasAccountByNickname(String nickname) throws BaseCahHandler.CahException {
        return getAccountSetByNickname(nickname) != null;
    }

    private void addAccount(UserAccount account) throws BaseCahHandler.CahException {
        try {
            int result = db.statement().executeUpdate("INSERT INTO users (username, auth, email, password) VALUES ('"
                    + account.username + "', '"
                    + Consts.AuthType.PASSWORD.toString() + "', '"
                    + account.email + "', '"
                    + account.hashedPassword + "')");

            if (result != 1) throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR);
        } catch (SQLException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR, ex);
        }
    }

    private void addGoogleAccount(UserAccount account, GoogleIdToken.Payload token) throws BaseCahHandler.CahException {
        try {
            int result = db.statement().executeUpdate("INSERT INTO users (username, auth, email, google_sub) VALUES ('"
                    + account.username + "', '"
                    + Consts.AuthType.GOOGLE.toString() + "', '"
                    + account.email + "', '"
                    + token.getSubject() + "')");

            if (result != 1) throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR);
        } catch (SQLException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR, ex);
        }
    }

    @NotNull
    public UserAccount registerWithPassword(String nickname, String email, String password) throws BaseCahHandler.CahException {
        UserAccount account = new UserAccount(nickname, email, Consts.AuthType.PASSWORD, BCrypt.hashpw(password, BCrypt.gensalt()));
        addAccount(account);
        return account;
    }

    @NotNull
    public UserAccount registerWithGoogle(String nickname, GoogleIdToken.Payload token) throws BaseCahHandler.CahException {
        UserAccount account = new UserAccount(nickname, token.getEmail(), Consts.AuthType.GOOGLE, null);
        addGoogleAccount(account, token);
        return account;
    }
}
