package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.DisconnectReason;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Parameters;
import net.socialgamer.cah.servlets.Sessions;

public class LogoutHandler extends BaseHandler {
    public final static String OP = AjaxOperation.LOG_OUT.toString();
    private final ConnectedUsers users;

    public LogoutHandler(final ConnectedUsers users) {
        this.users = users;
    }

    @Override
    public JsonElement handle(User user, Parameters params, NanoHTTPD.IHTTPSession session) {
        user.noLongerValid();
        users.removeUser(user, DisconnectReason.MANUAL);
        Sessions.invalidate(user.getSessionId());
        return new JsonObject();
    }
}
