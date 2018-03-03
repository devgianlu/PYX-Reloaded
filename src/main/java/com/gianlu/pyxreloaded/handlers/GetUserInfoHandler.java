package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.BaseJsonHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.UsersWithAccount;
import io.undertow.server.HttpServerExchange;

public class GetUserInfoHandler extends BaseHandler {
    public static final String OP = Consts.Operation.GET_USER_INFO.toString();
    private final UsersWithAccount accounts;

    public GetUserInfoHandler(@Annotations.UsersWithAccount UsersWithAccount accounts) {
        this.accounts = accounts;
    }

    @Override
    public JsonWrapper handle(User requester, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        String nickname = params.getStringNotNull(Consts.GeneralKeys.NICKNAME);

        if (!accounts.hasNickname(nickname)) throw new BaseCahHandler.CahException(Consts.ErrorCode.NO_SUCH_USER);

        return new JsonWrapper(Consts.GeneralKeys.NICKNAME, nickname);
    }
}
