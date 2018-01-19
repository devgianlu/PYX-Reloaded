package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxRequest;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.JsonWrapper;
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
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        if (BanList.contains(exchange.getHostName())) throw new BaseCahHandler.CahException(ErrorCode.BANNED);

        String nickname = params.get(AjaxRequest.NICKNAME);
        if (nickname == null) throw new BaseCahHandler.CahException(ErrorCode.NO_NICK_SPECIFIED);
        if (!Pattern.matches(VALID_NAME_PATTERN, nickname))
            throw new BaseCahHandler.CahException(ErrorCode.INVALID_NICK);
        if (nickname.equalsIgnoreCase("xyzzy"))
            throw new BaseCahHandler.CahException(ErrorCode.RESERVED_NICK);

        String pid = params.get(AjaxRequest.PERSISTENT_ID);
        if (pid == null || pid.isEmpty()) pid = UniqueIds.getNewRandomID();

        boolean admin;
        String adminToken = params.get(Constants.AjaxRequest.ADMIN_TOKEN);
        admin = adminToken != null && adminToken.length() == AdminToken.TOKEN_LENGTH && AdminToken.current().equals(adminToken);

        user = userFactory.create(nickname, exchange.getHostName(), admin, pid);

        ErrorCode errorCode = users.checkAndAdd(user);
        if (errorCode == null) {
            exchange.setResponseCookie(new CookieImpl("PYX-Session", Sessions.add(user)));

            JsonWrapper obj = new JsonWrapper();
            obj.add(AjaxResponse.NICKNAME, nickname);
            obj.add(AjaxResponse.IS_ADMIN, admin);
            obj.add(AjaxResponse.PERSISTENT_ID, pid);
            return obj;
        } else {
            throw new BaseCahHandler.CahException(errorCode);
        }
    }
}
