package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import net.socialgamer.cah.Constants.*;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.QueuedMessage;
import net.socialgamer.cah.data.QueuedMessage.MessageType;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class KickHandler extends AdminHandler {
    public static final String OP = AjaxOperation.KICK.toString();
    protected final Logger logger = Logger.getLogger(KickHandler.class);
    private final ConnectedUsers connectedUsers;

    @Inject
    public KickHandler(final ConnectedUsers connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    @Override
    public JsonElement handleAsAdmin(User user, Parameters params) throws CahResponder.CahException {
        String nickname = params.getFirst(AjaxRequest.NICKNAME);
        if (nickname == null || nickname.isEmpty()) throw new CahResponder.CahException(ErrorCode.NO_NICK_SPECIFIED);

        final User kickUser = connectedUsers.getUser(nickname);
        if (null == kickUser) throw new CahResponder.CahException(ErrorCode.NO_SUCH_USER);

        final Map<ReturnableData, Object> kickData = new HashMap<ReturnableData, Object>();
        kickData.put(LongPollResponse.EVENT, LongPollEvent.KICKED.toString());
        final QueuedMessage qm = new QueuedMessage(MessageType.KICKED, kickData);
        kickUser.enqueueMessage(qm);

        connectedUsers.removeUser(kickUser, DisconnectReason.KICKED);
        logger.warn(String.format("Kicking %s by request of %s", kickUser.getNickname(), user.getNickname()));

        return new JsonObject();
    }
}
