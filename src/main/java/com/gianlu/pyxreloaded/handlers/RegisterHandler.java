package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.data.UserAccount;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.BaseJsonHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.BanList;
import com.gianlu.pyxreloaded.singletons.ConnectedUsers;
import com.gianlu.pyxreloaded.singletons.Sessions;
import com.gianlu.pyxreloaded.singletons.UsersWithAccount;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import org.mindrot.jbcrypt.BCrypt;

import java.util.regex.Pattern;

public class RegisterHandler extends BaseHandler {
    public static final String OP = Consts.Operation.REGISTER.toString();
    private final BanList banList;
    private final UsersWithAccount accounts;
    private final ConnectedUsers users;

    public RegisterHandler(@Annotations.BanList BanList banList, @Annotations.UsersWithAccount UsersWithAccount accounts, @Annotations.ConnectedUsers ConnectedUsers users) {
        this.banList = banList;
        this.accounts = accounts;
        this.users = users;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        if (banList.contains(exchange.getHostName()))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.BANNED);

        String nickname = params.get(Consts.GeneralKeys.NICKNAME);
        if (nickname == null)
            throw new BaseCahHandler.CahException(Consts.ErrorCode.NO_NICK_SPECIFIED);
        if (nickname.equalsIgnoreCase("xyzzy"))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.RESERVED_NICK);
        if (!Pattern.matches(Consts.VALID_NAME_PATTERN, nickname))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.INVALID_NICK);


        UserAccount account = accounts.getAccount(nickname);
        if (account == null) { // Without account
            user = new User(nickname, exchange.getHostName(), Sessions.generateNewId());
        } else {
            switch (account.auth) {
                case PASSWORD:
                    String password = params.get(Consts.AuthType.PASSWORD);
                    if (password == null || password.isEmpty() || !BCrypt.checkpw(password, account.hashedPassword))
                        throw new BaseCahHandler.CahException(Consts.ErrorCode.WRONG_PASSWORD);

                    user = User.withAccount(account, exchange.getHostName());
                    break;
                case GOOGLE: // TODO
                    break;
                case FACEBOOK: // TODO
                    break;
                case TWITTER: // TODO
                    break;
                default:
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);
            }
        }

        users.checkAndAdd(user);
        exchange.setResponseCookie(new CookieImpl("PYX-Session", Sessions.get().add(user)));

        return new JsonWrapper()
                .add(Consts.GeneralKeys.NICKNAME, nickname)
                .add(Consts.GeneralKeys.IS_ADMIN, user.isAdmin());
    }
}
