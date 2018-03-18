package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.ConnectedUsers;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

public class BanHandler extends BaseHandler {
    public static final String OP = Consts.Operation.BAN.toString();
    private final ConnectedUsers connectedUsers;

    public BanHandler(@Annotations.ConnectedUsers ConnectedUsers connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    @NotNull
    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (!user.isAdmin()) throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_ADMIN);

        String nickname = params.getStringNotNull(Consts.UserData.NICKNAME);
        if (nickname.isEmpty()) throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

        User target = connectedUsers.getUser(nickname);
        if (target == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.NO_SUCH_USER);
        connectedUsers.banUser(target);

        return JsonWrapper.EMPTY;
    }
}
