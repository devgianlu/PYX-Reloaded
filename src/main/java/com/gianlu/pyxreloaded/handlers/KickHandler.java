package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.EventWrapper;
import com.gianlu.pyxreloaded.JsonWrapper;
import com.gianlu.pyxreloaded.data.ConnectedUsers;
import com.gianlu.pyxreloaded.data.QueuedMessage;
import com.gianlu.pyxreloaded.data.QueuedMessage.MessageType;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.servlets.Annotations;
import com.gianlu.pyxreloaded.servlets.BaseCahHandler;
import com.gianlu.pyxreloaded.servlets.Parameters;
import io.undertow.server.HttpServerExchange;
import org.apache.log4j.Logger;

public class KickHandler extends BaseHandler {
    public static final String OP = Consts.Operation.KICK.toString();
    protected final Logger logger = Logger.getLogger(KickHandler.class);
    private final ConnectedUsers connectedUsers;

    public KickHandler(@Annotations.ConnectedUsers ConnectedUsers connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (!user.isAdmin()) throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_ADMIN);

        String nickname = params.get(Consts.GeneralKeys.NICKNAME);
        if (nickname == null || nickname.isEmpty())
            throw new BaseCahHandler.CahException(Consts.ErrorCode.NO_NICK_SPECIFIED);

        final User kickUser = connectedUsers.getUser(nickname);
        if (kickUser == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.NO_SUCH_USER);

        kickUser.enqueueMessage(new QueuedMessage(MessageType.KICKED, new EventWrapper(Consts.Event.KICKED)));

        connectedUsers.removeUser(kickUser, Consts.DisconnectReason.KICKED);
        logger.warn(String.format("Kicking %s by request of %s", kickUser.getNickname(), user.getNickname()));

        return JsonWrapper.EMPTY;
    }
}
