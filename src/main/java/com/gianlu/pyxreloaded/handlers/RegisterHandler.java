package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.data.UserAccount;
import com.gianlu.pyxreloaded.google.GoogleTokenVerifierService;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.BaseJsonHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.BanList;
import com.gianlu.pyxreloaded.singletons.ConnectedUsers;
import com.gianlu.pyxreloaded.singletons.Sessions;
import com.gianlu.pyxreloaded.singletons.UsersWithAccount;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import org.mindrot.jbcrypt.BCrypt;

import java.util.regex.Pattern;

public class RegisterHandler extends BaseHandler {
    public static final String OP = Consts.Operation.REGISTER.toString();
    private final BanList banList;
    private final UsersWithAccount accounts;
    private final ConnectedUsers users;
    private final GoogleTokenVerifierService googleVerifier;

    public RegisterHandler(@Annotations.BanList BanList banList,
                           @Annotations.UsersWithAccount UsersWithAccount accounts,
                           @Annotations.ConnectedUsers ConnectedUsers users,
                           @Annotations.GoogleTokenVerifier GoogleTokenVerifierService googleVerifier) {
        this.banList = banList;
        this.accounts = accounts;
        this.users = users;
        this.googleVerifier = googleVerifier;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        if (banList.contains(exchange.getHostName()))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.BANNED);

        UserAccount account;
        String nickname;
        Consts.AuthType type = Consts.AuthType.parse(params.get(Consts.GeneralKeys.AUTH_TYPE));
        switch (type) {
            case PASSWORD:
                nickname = params.get(Consts.GeneralKeys.NICKNAME);
                if (nickname == null)
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.NO_NICK_SPECIFIED);
                if (nickname.equalsIgnoreCase("xyzzy"))
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.RESERVED_NICK);
                if (!Pattern.matches(Consts.VALID_NAME_PATTERN, nickname))
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.INVALID_NICK);

                account = accounts.getAccountByNickname(nickname);
                if (account == null) { // Without account
                    user = new User(nickname, exchange.getHostName(), Sessions.generateNewId());
                } else {
                    String password = params.get(Consts.AuthType.PASSWORD);
                    if (password == null || password.isEmpty() || !BCrypt.checkpw(password, account.hashedPassword))
                        throw new BaseCahHandler.CahException(Consts.ErrorCode.WRONG_PASSWORD);

                    user = User.withAccount(account, exchange.getHostName());
                }
                break;
            case GOOGLE:
                String tokenStr = params.get(Consts.AuthType.GOOGLE);
                GoogleIdToken.Payload token = googleVerifier.verify(tokenStr);
                if (token == null)
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

                account = accounts.getGoogleAccount(token.getSubject());
                if (account == null || account.auth != Consts.AuthType.GOOGLE)
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

                nickname = account.username;
                break;
            default:
            case FACEBOOK:
            case TWITTER:
                throw new UnsupportedOperationException();
        }

        users.checkAndAdd(user);
        exchange.setResponseCookie(new CookieImpl("PYX-Session", Sessions.get().add(user)));

        return new JsonWrapper()
                .add(Consts.GeneralKeys.NICKNAME, nickname)
                .add(Consts.GeneralKeys.IS_ADMIN, user.isAdmin());
    }
}
