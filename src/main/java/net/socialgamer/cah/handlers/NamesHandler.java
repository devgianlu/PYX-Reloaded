package net.socialgamer.cah.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.Utils;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Parameters;

public class NamesHandler extends BaseHandler {
    public static final String OP = AjaxOperation.NAMES.toString();
    private final ConnectedUsers users;

    public NamesHandler(final ConnectedUsers users) {
        this.users = users;
    }

    @Override
    public JsonElement handle(User user, Parameters params, NanoHTTPD.IHTTPSession session) {
        JsonArray array = new JsonArray();
        for (User item : users.getUsers()) array.add(item.getNickname());
        return Utils.singletonJsonObject(AjaxResponse.NAMES.toString(), array);
    }
}
