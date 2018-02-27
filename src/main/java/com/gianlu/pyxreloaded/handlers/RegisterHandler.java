package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.data.accounts.PasswordAccount;
import com.gianlu.pyxreloaded.data.accounts.UserAccount;
import com.gianlu.pyxreloaded.facebook.FacebookToken;
import com.gianlu.pyxreloaded.github.GithubProfileInfo;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.BaseJsonHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import org.jetbrains.annotations.Nullable;
import org.mindrot.jbcrypt.BCrypt;

import java.util.regex.Pattern;

public class RegisterHandler extends BaseHandler {
    public static final String OP = Consts.Operation.REGISTER.toString();
    private final BanList banList;
    private final UsersWithAccount accounts;
    private final ConnectedUsers users;
    private final SocialLogin socialLogin;

    public RegisterHandler(@Annotations.BanList BanList banList,
                           @Annotations.UsersWithAccount UsersWithAccount accounts,
                           @Annotations.ConnectedUsers ConnectedUsers users,
                           @Annotations.SocialLogin SocialLogin socialLogin) {
        this.banList = banList;
        this.accounts = accounts;
        this.users = users;
        this.socialLogin = socialLogin;
    }

    @Override
    public JsonWrapper handle(@Nullable User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        if (banList.contains(exchange.getHostName()))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.BANNED);

        UserAccount account;
        String nickname;
        Consts.AuthType type = Consts.AuthType.parse(params.get(Consts.GeneralKeys.AUTH_TYPE));
        switch (type) {
            case PASSWORD:
                nickname = params.get(Consts.GeneralKeys.NICKNAME);
                if (nickname == null)
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);
                if (!Pattern.matches(Consts.VALID_NAME_PATTERN, nickname))
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.INVALID_NICK);

                account = accounts.getPasswordAccount(nickname);
                if (account == null) { // Without account
                    user = new User(nickname, exchange.getHostName(), Sessions.generateNewId());
                } else {
                    String password = params.get(Consts.AuthType.PASSWORD);
                    if (password == null || password.isEmpty() || !BCrypt.checkpw(password, ((PasswordAccount) account).hashedPassword))
                        throw new BaseCahHandler.CahException(Consts.ErrorCode.WRONG_PASSWORD);

                    user = User.withAccount(account, exchange.getHostName());
                }
                break;
            case GOOGLE:
                GoogleIdToken.Payload googleToken = socialLogin.verifyGoogle(params.get(Consts.AuthType.GOOGLE));
                if (googleToken == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.GOOGLE_INVALID_TOKEN);

                account = accounts.getGoogleAccount(googleToken.getSubject());
                if (account == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.GOOGLE_NOT_REGISTERED);

                nickname = account.username;
                user = User.withAccount(account, exchange.getHostName());
                break;
            case FACEBOOK:
                FacebookToken facebookToken = socialLogin.verifyFacebook(params.get(Consts.AuthType.FACEBOOK));
                if (facebookToken == null)
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.FACEBOOK_INVALID_TOKEN);

                account = accounts.getFacebookAccount(facebookToken.userId);
                if (account == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.FACEBOOK_NOT_REGISTERED);

                nickname = account.username;
                user = User.withAccount(account, exchange.getHostName());
                break;
            case GITHUB:
                String githubToken = params.get(Consts.AuthType.GITHUB);
                if (githubToken == null)
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.GITHUB_INVALID_TOKEN);

                GithubProfileInfo githubInfo = socialLogin.infoGithub(githubToken);
                account = accounts.getGithubAccount(githubInfo.id);
                if (account == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.GITHUB_NOT_REGISTERED);

                nickname = account.username;
                user = User.withAccount(account, exchange.getHostName());
                break;
            default:
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
