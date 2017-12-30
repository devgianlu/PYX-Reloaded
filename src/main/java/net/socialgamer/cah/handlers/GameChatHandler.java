package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.Constants.*;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.QueuedMessage.MessageType;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;

import java.util.HashMap;

public class GameChatHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.GAME_CHAT.toString();

    @Inject
    public GameChatHandler(final GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonElement handleWithUserInGame(User user, Game game, Parameters params, NanoHTTPD.IHTTPSession session) throws CahResponder.CahException {
        if (user.getLastMessageTimes().size() >= Constants.CHAT_FLOOD_MESSAGE_COUNT) {
            final Long head = user.getLastMessageTimes().get(0);
            if (System.currentTimeMillis() - head < Constants.CHAT_FLOOD_TIME)
                throw new CahResponder.CahException(ErrorCode.TOO_FAST);

            user.getLastMessageTimes().remove(0);
        }

        String msg = params.getFirst(AjaxRequest.MESSAGE);
        if (msg == null || msg.isEmpty()) throw new CahResponder.CahException(ErrorCode.NO_MSG_SPECIFIED);

        boolean emote = params.getFirstBoolean(AjaxRequest.EMOTE, false);

        if (msg.length() > Constants.CHAT_MAX_LENGTH) {
            throw new CahResponder.CahException(ErrorCode.MESSAGE_TOO_LONG);
        } else {
            user.getLastMessageTimes().add(System.currentTimeMillis());
            final HashMap<ReturnableData, Object> broadcastData = new HashMap<ReturnableData, Object>();
            broadcastData.put(LongPollResponse.EVENT, LongPollEvent.CHAT.toString());
            broadcastData.put(LongPollResponse.FROM, user.getNickname());
            broadcastData.put(LongPollResponse.MESSAGE, msg);
            broadcastData.put(LongPollResponse.FROM_ADMIN, user.isAdmin());
            broadcastData.put(LongPollResponse.GAME_ID, game.getId());
            broadcastData.put(LongPollResponse.EMOTE, emote);
            game.broadcastToPlayers(MessageType.CHAT, broadcastData);
        }

        return new JsonObject();
    }
}
