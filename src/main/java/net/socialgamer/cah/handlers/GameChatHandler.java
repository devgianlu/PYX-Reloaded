package net.socialgamer.cah.handlers;

import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.Constants.*;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.QueuedMessage.MessageType;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.BaseCahHandler;
import net.socialgamer.cah.servlets.Parameters;

public class GameChatHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.GAME_CHAT.toString();

    public GameChatHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        user.checkChatFlood();

        String msg = params.get(AjaxRequest.MESSAGE);
        if (msg == null || msg.isEmpty()) throw new BaseCahHandler.CahException(ErrorCode.NO_MSG_SPECIFIED);

        boolean emote = params.getBoolean(AjaxRequest.EMOTE, false);

        if (msg.length() > Constants.CHAT_MAX_LENGTH) {
            throw new BaseCahHandler.CahException(ErrorCode.MESSAGE_TOO_LONG);
        } else {
            user.getLastMessageTimes().add(System.currentTimeMillis());
            JsonObject obj = new JsonObject();
            obj.addProperty(LongPollResponse.EVENT.toString(), LongPollEvent.CHAT.toString());
            obj.addProperty(LongPollResponse.FROM.toString(), user.getNickname());
            obj.addProperty(LongPollResponse.MESSAGE.toString(), msg);
            obj.addProperty(LongPollResponse.FROM_ADMIN.toString(), user.isAdmin());
            obj.addProperty(LongPollResponse.GAME_ID.toString(), game.getId());
            obj.addProperty(LongPollResponse.EMOTE.toString(), emote);
            game.broadcastToPlayers(MessageType.CHAT, obj);
        }

        return JsonWrapper.EMPTY;
    }
}
