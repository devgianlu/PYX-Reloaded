package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants.*;
import net.socialgamer.cah.Utils;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.QueuedMessage;
import net.socialgamer.cah.data.QueuedMessage.MessageType;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.BanList;
import net.socialgamer.cah.servlets.BaseCahHandler;
import net.socialgamer.cah.servlets.Parameters;
import org.apache.log4j.Logger;

public class BanHandler extends BaseHandler {
    public static final String OP = AjaxOperation.BAN.toString();
    protected final Logger logger = Logger.getLogger(BanHandler.class);
    private final ConnectedUsers connectedUsers;

    public BanHandler(@Annotations.ConnectedUsers ConnectedUsers connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    @Override
    public JsonElement handle(User user, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (!user.isAdmin()) throw new BaseCahHandler.CahException(ErrorCode.NOT_ADMIN);

        String nickname = params.get(AjaxRequest.NICKNAME);
        if (nickname == null || nickname.isEmpty())
            throw new BaseCahHandler.CahException(ErrorCode.NO_NICK_SPECIFIED);

        String banIp;
        User kickUser = connectedUsers.getUser(nickname);
        if (kickUser != null) {
            banIp = kickUser.getHostname();

            kickUser.enqueueMessage(new QueuedMessage(MessageType.KICKED, Utils.singletonJsonObject(LongPollResponse.EVENT.toString(), LongPollEvent.BANNED.toString())));

            connectedUsers.removeUser(kickUser, DisconnectReason.BANNED);
            logger.info(String.format("Banning %s (%s) by request of %s", kickUser.getNickname(), banIp, user.getNickname()));
        } else {
            banIp = nickname;
            logger.info(String.format("Banning %s by request of %s", banIp, user.getNickname()));
        }

        BanList.add(banIp);
        return new JsonObject();
    }
}
