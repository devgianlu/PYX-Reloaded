package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.Constants.*;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.QueuedMessage.MessageType;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.BaseUriResponder;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;

public class ChatHandler extends BaseHandler {
    public static final String OP = AjaxOperation.CHAT.toString();
    private final ConnectedUsers users;

    public ChatHandler(@Annotations.ConnectedUsers ConnectedUsers users) {
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

        String msg = params.get(AjaxRequest.MESSAGE);
        if (msg == null || msg.isEmpty()) throw new CahResponder.CahException(ErrorCode.NO_MSG_SPECIFIED);
        if (!user.isAdmin()) throw new CahResponder.CahException(ErrorCode.NOT_ADMIN);

        boolean wall = params.getBoolean(AjaxRequest.WALL, false);
        boolean emote = params.getBoolean(AjaxRequest.EMOTE, false);

        if (msg.length() > Constants.CHAT_MAX_LENGTH) {
            throw new CahResponder.CahException(ErrorCode.MESSAGE_TOO_LONG);
        } else {
            user.getLastMessageTimes().add(System.currentTimeMillis());
            JsonObject obj = new JsonObject();
            obj.addProperty(LongPollResponse.EVENT.toString(), LongPollEvent.CHAT.toString());
            obj.addProperty(LongPollResponse.FROM.toString(), user.getNickname());
            obj.addProperty(LongPollResponse.MESSAGE.toString(), msg);
            if (user.isAdmin()) obj.addProperty(LongPollResponse.FROM_ADMIN.toString(), true);
            if (wall) obj.addProperty(LongPollResponse.WALL.toString(), true);
            if (emote) obj.addProperty(LongPollResponse.EMOTE.toString(), true);
            users.broadcastToAll(MessageType.CHAT, obj);
        }

        return new JsonObject();
    }
}
