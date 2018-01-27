/*
 * Handler to ban users via either nick or IP.
 */
package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants.*;
import net.socialgamer.cah.JsonWrapper;
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
    private final ConnectedUsers connectedUsers; //Presumably between here to line 28, we get the list of users currently connected.

    public BanHandler(@Annotations.ConnectedUsers ConnectedUsers connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (!user.isAdmin()) throw new BaseCahHandler.CahException(ErrorCode.NOT_ADMIN); //Detect that user doesn't have permission to kick/ban etc

        String nickname = params.get(AjaxRequest.NICKNAME); //Set a variable "nickname" to the one entered through the command.
        
        //Assuming this is for when the command wasn't properly typed
        if (nickname == null || nickname.isEmpty())
            throw new BaseCahHandler.CahException(ErrorCode.NO_NICK_SPECIFIED);

        String banIp;
        User kickUser = connectedUsers.getUser(nickname); //Single out the user we want to ban, give it its own object
        
        /*
         * Assuming singled out user exists, set banIP to the IP(?) of the user in question.
         * Then, send a message via the LongPoll servlet to the client with a notif they've been kicked.
         * Remove the user in question from the list of connected users on the server
         * To everyone else: Send a message of who banned who
         */
        if (kickUser != null) {
            banIp = kickUser.getHostname();

            kickUser.enqueueMessage(new QueuedMessage(MessageType.KICKED, Utils.singletonJsonObject(LongPollResponse.EVENT.toString(), LongPollEvent.BANNED.toString())));

            connectedUsers.removeUser(kickUser, DisconnectReason.BANNED);
            logger.info(String.format("Banning %s (%s) by request of %s", kickUser.getNickname(), banIp, user.getNickname()));
        } else { //Ban via nickname instead of IP address.
            banIp = nickname;
            logger.info(String.format("Banning %s by request of %s", banIp, user.getNickname()));
        }

        BanList.add(banIp); //Whatever banIp was determined to be, it was documented server-side right here.
        return JsonWrapper.EMPTY; //Doesn't return any JSON
    }
}
