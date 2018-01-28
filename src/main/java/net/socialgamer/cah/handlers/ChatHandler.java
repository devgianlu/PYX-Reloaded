package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.Constants.*;
import net.socialgamer.cah.EventWrapper;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.QueuedMessage.MessageType;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.BaseCahHandler;
import net.socialgamer.cah.servlets.BaseJsonHandler;
import net.socialgamer.cah.servlets.Parameters;

public class ChatHandler extends BaseHandler {
    public static final String OP = AjaxOperation.CHAT.toString();
    private final ConnectedUsers users;

    public ChatHandler(@Annotations.ConnectedUsers ConnectedUsers users) {
        this.users = users;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        user.checkChatFlood();

        String msg = params.get(AjaxRequest.MESSAGE);
        if (msg == null || msg.isEmpty()) throw new BaseCahHandler.CahException(ErrorCode.NO_MSG_SPECIFIED);
        if (!user.isAdmin()) throw new BaseCahHandler.CahException(ErrorCode.NOT_ADMIN);

        boolean wall = params.getBoolean(AjaxRequest.WALL, false);
        boolean emote = params.getBoolean(AjaxRequest.EMOTE, false);

        if (msg.length() > Constants.CHAT_MAX_LENGTH) {
            throw new BaseCahHandler.CahException(ErrorCode.MESSAGE_TOO_LONG);
        } else {
            user.getLastMessageTimes().add(System.currentTimeMillis());
            EventWrapper ev = new EventWrapper(LongPollEvent.CHAT);
            ev.add(LongPollResponse.FROM, user.getNickname());
            ev.add(LongPollResponse.MESSAGE, msg);
            if (user.isAdmin()) ev.add(LongPollResponse.FROM_ADMIN, true);
            if (wall) ev.add(LongPollResponse.WALL, true);
            if (emote) ev.add(LongPollResponse.EMOTE, true);
            users.broadcastToAll(MessageType.CHAT, ev);
        }

        return JsonWrapper.EMPTY;
    }
}
