package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.data.accounts.UserAccount;
import com.gianlu.pyxreloaded.server.Parameters;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

public class GetUserPreferencesHandler extends BaseHandler {
    public static final String OP = Consts.Operation.GET_USER_PREFERENCES.toString();

    @NotNull
    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) {
        UserAccount account = user.getAccount();
        if (account == null) return JsonWrapper.EMPTY;
        else return account.preferences.toJson();
    }
}
