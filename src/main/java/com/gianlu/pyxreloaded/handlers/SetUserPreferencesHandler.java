package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.Utils;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.data.accounts.UserAccount;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.UsersWithAccount;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SetUserPreferencesHandler extends BaseHandler {
    public static final String OP = Consts.Operation.SET_USER_PREFERENCES.toString();
    private final JsonParser parser = new JsonParser();
    private final UsersWithAccount accounts;

    public SetUserPreferencesHandler(@Annotations.UsersWithAccount UsersWithAccount accounts) {
        this.accounts = accounts;
    }

    @NotNull
    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        UserAccount account = user.getAccount();
        if (account == null) return JsonWrapper.EMPTY;

        try {
            JsonObject obj = parser.parse(params.getStringNotNull(Consts.GeneralKeys.USER_PREFERENCES)).getAsJsonObject();
            Map<String, String> map = Utils.toMap(obj);
            account.updatePreferences(map);
            accounts.updatePreferences(account, map);
        } catch (IllegalStateException | JsonSyntaxException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST, ex);
        }

        return account.preferences.toJson();
    }
}
