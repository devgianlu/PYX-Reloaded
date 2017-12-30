package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.Constants.*;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.QueuedMessage.MessageType;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.BaseUriResponder;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;

import java.util.HashMap;

public class ChatHandler extends BaseHandler {
    public static final String OP = AjaxOperation.CHAT.toString();
    private final ConnectedUsers users;

    @Inject
    public ChatHandler(final ConnectedUsers users) {
        this.users = users;
    }

    @Override
    public JsonElement handle(User user, Parameters params, NanoHTTPD.IHTTPSession session) throws BaseUriResponder.StatusException {
        if (user.getLastMessageTimes().size() >= Constants.CHAT_FLOOD_MESSAGE_COUNT) {
            final Long head = user.getLastMessageTimes().get(0);
            if (System.currentTimeMillis() - head < Constants.CHAT_FLOOD_TIME)
                throw new CahResponder.CahException(ErrorCode.TOO_FAST);

            user.getLastMessageTimes().remove(0);
        }

        String msg = params.getFirst(AjaxRequest.MESSAGE);
        if (msg == null || msg.isEmpty()) throw new CahResponder.CahException(ErrorCode.NO_MSG_SPECIFIED);
        if (!user.isAdmin()) throw new CahResponder.CahException(ErrorCode.NOT_ADMIN);

        boolean wall = params.getFirstBoolean(AjaxRequest.WALL, false);
        boolean emote = params.getFirstBoolean(AjaxRequest.EMOTE, false);

        if (msg.length() > Constants.CHAT_MAX_LENGTH) {
            throw new CahResponder.CahException(ErrorCode.MESSAGE_TOO_LONG);
        } else {
            user.getLastMessageTimes().add(System.currentTimeMillis());
            final HashMap<ReturnableData, Object> broadcastData = new HashMap<ReturnableData, Object>();
            broadcastData.put(LongPollResponse.EVENT, LongPollEvent.CHAT.toString());
            broadcastData.put(LongPollResponse.FROM, user.getNickname());
            broadcastData.put(LongPollResponse.MESSAGE, msg);
            if (user.isAdmin()) broadcastData.put(LongPollResponse.FROM_ADMIN, true);
            if (wall) broadcastData.put(LongPollResponse.WALL, true);
            if (emote) broadcastData.put(LongPollResponse.EMOTE, true);
            users.broadcastToAll(MessageType.CHAT, broadcastData);
        }

        return new JsonObject();
    }
}
