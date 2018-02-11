package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.data.ConnectedUsers;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.*;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;

import java.util.regex.Pattern;

public class RegisterHandler extends BaseHandler {
    public static final String OP = Consts.Operation.REGISTER.toString();
    private static final String VALID_NAME_PATTERN = "[a-zA-Z_][a-zA-Z0-9_]{2,29}";
    private final ConnectedUsers users;

    public RegisterHandler(@Annotations.ConnectedUsers ConnectedUsers users) {
        this.users = users;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        if (BanList.contains(exchange.getHostName())) throw new BaseCahHandler.CahException(Consts.ErrorCode.BANNED);

        String nickname = params.get(Consts.GeneralKeys.NICKNAME);
        if (nickname == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.NO_NICK_SPECIFIED);
        if (!Pattern.matches(VALID_NAME_PATTERN, nickname))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.INVALID_NICK);
        if (nickname.equalsIgnoreCase("xyzzy"))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.RESERVED_NICK);

        boolean admin;
        String adminToken = params.get(Consts.GeneralKeys.ADMIN_TOKEN);
        admin = adminToken != null && adminToken.length() == AdminToken.TOKEN_LENGTH && AdminToken.current().equals(adminToken);

        user = new User(nickname, exchange.getHostName(), Sessions.generateNewId(), admin);
        users.checkAndAdd(user);
        exchange.setResponseCookie(new CookieImpl("PYX-Session", Sessions.add(user)));

        return new JsonWrapper()
                .add(Consts.GeneralKeys.NICKNAME, nickname)
                .add(Consts.GeneralKeys.IS_ADMIN, admin);
    }
}
