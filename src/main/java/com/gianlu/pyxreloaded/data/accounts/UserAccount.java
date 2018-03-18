package com.gianlu.pyxreloaded.data.accounts;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public abstract class UserAccount {
    public final String username;
    public final boolean admin;
    public final String email;
    public final String avatarUrl;
    public final boolean emailVerified;
    public final Preferences preferences = new Preferences();
    private final Consts.AuthType auth;

    UserAccount(ResultSet set, boolean emailVerified) throws SQLException, ParseException {
        this.username = set.getString("username");
        this.email = set.getString("email");
        this.auth = Consts.AuthType.parse(set.getString("auth"));
        this.admin = set.getBoolean("admin");
        this.avatarUrl = set.getString("avatar_url");
        this.emailVerified = emailVerified;
    }

    UserAccount(String username, String email, Consts.AuthType auth, boolean emailVerified, @Nullable String avatarUrl) {
        this.username = username;
        this.email = email;
        this.auth = auth;
        this.avatarUrl = avatarUrl;
        this.admin = false;
        this.emailVerified = emailVerified;
    }

    public JsonWrapper toJson() {
        JsonWrapper obj = new JsonWrapper();
        obj.add(Consts.UserData.EMAIL, email);
        obj.add(Consts.GeneralKeys.AUTH_TYPE, auth.toString());
        obj.add(Consts.UserData.PICTURE, avatarUrl);
        obj.add(Consts.UserData.NICKNAME, username);
        obj.add(Consts.UserData.EMAIL_VERIFIED, emailVerified);
        obj.add(Consts.UserData.IS_ADMIN, admin);
        return obj;
    }

    public void loadPreferences(@NotNull ResultSet prefs) throws SQLException {
        preferences.load(prefs);
    }

    public void updatePreferences(@NotNull Map<String, String> map) {
        preferences.update(map);
    }

    public class Preferences extends HashMap<String, String> {

        private Preferences() {
        }

        private void load(@NotNull ResultSet set) throws SQLException {
            while (set.next()) put(set.getString("key"), set.getString("value"));
        }

        @NotNull
        public JsonWrapper toJson() {
            return JsonWrapper.from(this);
        }

        private void update(@NotNull Map<String, String> map) {
            for (String key : map.keySet()) {
                if (Consts.isPreferenceKeyValid(key))
                    put(key, map.get(key));
            }
        }
    }
}
