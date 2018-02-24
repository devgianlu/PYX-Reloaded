package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.accounts.GoogleAccount;
import com.gianlu.pyxreloaded.data.accounts.PasswordAccount;
import com.gianlu.pyxreloaded.data.accounts.UserAccount;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
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
            throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR, ex);
        }
    }

    @Nullable
    public GoogleAccount getGoogleAccount(@NotNull String subject) throws BaseCahHandler.CahException {
        try (ResultSet set = db.statement().executeQuery("SELECT * FROM users WHERE google_sub='" + subject + "'")) {
            return set.next() ? new GoogleAccount(set) : null;
        } catch (SQLException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR, ex);
        }
    }

    public boolean hasNickname(@NotNull String nickname) throws BaseCahHandler.CahException {
        try (ResultSet set = db.statement().executeQuery("SELECT count(*) FROM users WHERE username='" + nickname + "'")) {
            return set.getInt(1) > 0;
        } catch (SQLException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR, ex);
        }
    }

    private void addAccount(PasswordAccount account) throws BaseCahHandler.CahException {
        try (Statement statement = db.statement()) {
            int result = statement.executeUpdate("INSERT INTO users (username, auth, email, avatar_url, password) VALUES "
                    + VALUES(account.username, Consts.AuthType.PASSWORD.toString(), account.email, account.avatarUrl, account.hashedPassword));

            if (result != 1) throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR);
        } catch (SQLException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR, ex);
        }
    }

    private void addAccount(GoogleAccount account) throws BaseCahHandler.CahException {
        try (Statement statement = db.statement()) {
            int result = statement.executeUpdate("INSERT INTO users (username, auth, email, avatar_url, google_sub) VALUES " +
                    VALUES(account.username, Consts.AuthType.GOOGLE.toString(), account.email, account.avatarUrl, account.subject));

            if (result != 1) throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR);
        } catch (SQLException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.SQL_ERROR, ex);
        }
    }

    @NotNull
    public PasswordAccount registerWithPassword(String nickname, String email, String password) throws BaseCahHandler.CahException {
        PasswordAccount account = new PasswordAccount(nickname, email, BCrypt.hashpw(password, BCrypt.gensalt()));
        addAccount(account);
        return account;
    }

    @NotNull
    public UserAccount registerWithGoogle(String nickname, GoogleIdToken.Payload token) throws BaseCahHandler.CahException {
        GoogleAccount account = new GoogleAccount(nickname, token.getEmail(), token);
        addAccount(account);
        return account;
    }
}
