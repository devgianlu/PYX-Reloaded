package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxRequest;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.UniqueIds;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.*;

import java.util.regex.Pattern;

public class RegisterHandler extends BaseHandler {
    public static final String OP = AjaxOperation.REGISTER.toString();
    private static final String VALID_NAME_PATTERN = "[a-zA-Z_][a-zA-Z0-9_]{2,29}";
    private final ConnectedUsers users;
    private final User.Factory userFactory;

    public RegisterHandler(@Annotations.ConnectedUsers ConnectedUsers users, @Annotations.UserFactory User.Factory userFactory) {
        this.users = users;
        this.userFactory = userFactory;
    }

    @Override
    public JsonElement handle(User user, Parameters params, NanoHTTPD.IHTTPSession session) throws BaseUriResponder.StatusException {
        if (BanList.contains(session.getRemoteIpAddress())) throw new CahResponder.CahException(ErrorCode.BANNED);

        String nickname = params.get(AjaxRequest.NICKNAME);
        if (nickname == null) throw new CahResponder.CahException(ErrorCode.NO_NICK_SPECIFIED);
        if (!Pattern.matches(VALID_NAME_PATTERN, nickname)) throw new CahResponder.CahException(ErrorCode.INVALID_NICK);
        if (nickname.equalsIgnoreCase("xyzzy"))
            throw new CahResponder.CahException(ErrorCode.RESERVED_NICK);

        String pid = params.get(AjaxRequest.PERSISTENT_ID);
        if (pid == null || pid.isEmpty()) pid = UniqueIds.getNewRandomID();

        user = userFactory.create(nickname,
                session.getRemoteIpAddress(),
                Constants.ADMIN_IP_ADDRESSES.contains(session.getRemoteIpAddress()), pid);

        ErrorCode errorCode = users.checkAndAdd(user);
        if (errorCode == null) {
            setHeader("Set-Cookie", "PYX-Session=" + Sessions.add(user));

            JsonObject obj = new JsonObject();
            obj.addProperty(AjaxResponse.NICKNAME.toString(), nickname);
            obj.addProperty(AjaxResponse.PERSISTENT_ID.toString(), pid);
            return obj;
        } else {
            throw new CahResponder.CahException(errorCode);
        }
    }
}
