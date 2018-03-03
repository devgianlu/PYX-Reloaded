package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.EventWrapper;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.QueuedMessage.MessageType;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.BaseJsonHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.ConnectedUsers;
import io.undertow.server.HttpServerExchange;

public class ChatHandler extends BaseHandler {
    public static final String OP = Consts.Operation.CHAT.toString();
    private final ConnectedUsers users;

    public ChatHandler(@Annotations.ConnectedUsers ConnectedUsers users) {
        this.users = users;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        user.checkChatFlood();

        String msg = params.getStringNotNull(Consts.ChatData.MESSAGE);
        if (msg.isEmpty()) throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);
        if (!user.isAdmin()) throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_ADMIN);

        if (msg.length() > Consts.CHAT_MAX_LENGTH) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.MESSAGE_TOO_LONG);
        } else {
            user.getLastMessageTimes().add(System.currentTimeMillis());
            EventWrapper ev = new EventWrapper(Consts.Event.CHAT);
            ev.add(Consts.ChatData.FROM, user.getNickname());
            ev.add(Consts.ChatData.MESSAGE, msg);
            if (user.isAdmin()) ev.add(Consts.ChatData.FROM_ADMIN, true);
            users.broadcastToAll(MessageType.CHAT, ev);
        }

        return JsonWrapper.EMPTY;
    }
}
