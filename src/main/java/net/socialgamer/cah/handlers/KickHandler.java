package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants.*;
import net.socialgamer.cah.EventWrapper;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.QueuedMessage;
import net.socialgamer.cah.data.QueuedMessage.MessageType;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.BaseCahHandler;
import net.socialgamer.cah.servlets.Parameters;
import org.apache.log4j.Logger;

public class KickHandler extends BaseHandler {
    public static final String OP = AjaxOperation.KICK.toString();
    protected final Logger logger = Logger.getLogger(KickHandler.class);
    private final ConnectedUsers connectedUsers;

    public KickHandler(@Annotations.ConnectedUsers ConnectedUsers connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (!user.isAdmin()) throw new BaseCahHandler.CahException(ErrorCode.NOT_ADMIN);

        String nickname = params.get(AjaxRequest.NICKNAME);
        if (nickname == null || nickname.isEmpty()) throw new BaseCahHandler.CahException(ErrorCode.NO_NICK_SPECIFIED);

        final User kickUser = connectedUsers.getUser(nickname);
        if (kickUser == null) throw new BaseCahHandler.CahException(ErrorCode.NO_SUCH_USER);

        kickUser.enqueueMessage(new QueuedMessage(MessageType.KICKED, new EventWrapper(LongPollEvent.KICKED)));

        connectedUsers.removeUser(kickUser, DisconnectReason.KICKED);
        logger.warn(String.format("Kicking %s by request of %s", kickUser.getNickname(), user.getNickname()));

        return JsonWrapper.EMPTY;
    }
}
