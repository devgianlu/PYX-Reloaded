package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.EventWrapper;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.QueuedMessage.MessageType;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.ConnectedUsers;
import com.gianlu.pyxreloaded.singletons.GamesManager;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

public class GameChatHandler extends GameWithPlayerHandler {
    public static final String OP = Consts.Operation.GAME_CHAT.toString();
    private final ConnectedUsers users;

    public GameChatHandler(
            @Annotations.ConnectedUsers ConnectedUsers users,
            @Annotations.GameManager GamesManager gamesManager) {
        super(gamesManager);
        this.users = users;
    }

    @NotNull
    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        users.checkChatFlood(user);

        String msg = params.getStringNotNull(Consts.ChatData.MESSAGE);
        if (msg.isEmpty()) throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

        if (msg.length() > Consts.CHAT_MAX_LENGTH) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.MESSAGE_TOO_LONG);
        } else if (!users.runChatCommand(user, msg)) {
            user.getLastMessageTimes().add(System.currentTimeMillis());
            EventWrapper ev = new EventWrapper(game, Consts.Event.CHAT);
            ev.add(Consts.ChatData.FROM, user.getNickname());
            ev.add(Consts.ChatData.MESSAGE, msg);
            ev.add(Consts.GeneralKeys.GAME_ID, game.getId());
            ev.add(Consts.ChatData.FROM_ADMIN, user.isAdmin());
            game.broadcastToPlayers(MessageType.CHAT, ev);
        }

        return JsonWrapper.EMPTY;
    }
}
