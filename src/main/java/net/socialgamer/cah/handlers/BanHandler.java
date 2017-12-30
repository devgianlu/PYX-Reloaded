package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.socialgamer.cah.Constants.*;
import net.socialgamer.cah.Utils;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.QueuedMessage;
import net.socialgamer.cah.data.QueuedMessage.MessageType;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;
import org.apache.log4j.Logger;

import java.util.Set;

public class BanHandler extends AdminHandler {
    public static final String OP = AjaxOperation.BAN.toString();
    protected final Logger logger = Logger.getLogger(BanHandler.class);
    private final ConnectedUsers connectedUsers;
    private final Set<String> banList;

    public BanHandler(ConnectedUsers connectedUsers, Set<String> banList) {
        this.connectedUsers = connectedUsers;
        this.banList = banList;
    }

    @Override
    public JsonElement handleAsAdmin(User user, Parameters params) throws CahResponder.CahException {
        if (!user.isAdmin()) throw new CahResponder.CahException(ErrorCode.NOT_ADMIN);

        String nickname = params.get(AjaxRequest.NICKNAME);
        if (nickname == null || nickname.isEmpty())
            throw new CahResponder.CahException(ErrorCode.NO_NICK_SPECIFIED);

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

        banList.add(banIp);
        return new JsonObject();
    }
}
