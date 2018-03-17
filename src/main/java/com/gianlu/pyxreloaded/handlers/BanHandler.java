package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.EventWrapper;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.QueuedMessage;
import com.gianlu.pyxreloaded.data.QueuedMessage.MessageType;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.BanList;
import com.gianlu.pyxreloaded.singletons.ConnectedUsers;
import io.undertow.server.HttpServerExchange;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class BanHandler extends BaseHandler {
    public static final String OP = Consts.Operation.BAN.toString();
    protected final Logger logger = Logger.getLogger(BanHandler.class);
    private final BanList banList;
    private final ConnectedUsers connectedUsers;

    public BanHandler(@Annotations.BanList BanList banList, @Annotations.ConnectedUsers ConnectedUsers connectedUsers) {
        this.banList = banList;
        this.connectedUsers = connectedUsers;
    }

    @NotNull
    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (!user.isAdmin())
            throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_ADMIN);

        String nickname = params.getStringNotNull(Consts.UserData.NICKNAME);
        if (nickname.isEmpty()) throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

        User kickUser = connectedUsers.getUser(nickname);

        if (kickUser != null) {
            banList.add(kickUser.getHostname());

            kickUser.enqueueMessage(new QueuedMessage(MessageType.KICKED, new EventWrapper(Consts.Event.BANNED)));

            connectedUsers.removeUser(kickUser, Consts.DisconnectReason.BANNED);
            logger.info(String.format("Banning %s (%s) by request of %s", kickUser.getNickname(), kickUser.getHostname(), user.getNickname()));
        }

        return JsonWrapper.EMPTY;
    }
}
