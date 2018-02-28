package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.data.accounts.UserAccount;
import com.gianlu.pyxreloaded.facebook.FacebookProfileInfo;
import com.gianlu.pyxreloaded.facebook.FacebookToken;
import com.gianlu.pyxreloaded.github.GithubProfileInfo;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.BaseJsonHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.BanList;
import com.gianlu.pyxreloaded.singletons.ConnectedUsers;
import com.gianlu.pyxreloaded.singletons.SocialLogin;
import com.gianlu.pyxreloaded.singletons.UsersWithAccount;
import com.gianlu.pyxreloaded.twitter.TwitterProfileInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.undertow.server.HttpServerExchange;

import java.util.regex.Pattern;

public class CreateAccountHandler extends BaseHandler {
    public static final String OP = Consts.Operation.CREATE_ACCOUNT.toString();
    private final BanList banList;
    private final ConnectedUsers connectedUsers;
    private final UsersWithAccount accounts;
    private final SocialLogin socialLogin;

    public CreateAccountHandler(@Annotations.BanList BanList banList,
                                @Annotations.ConnectedUsers ConnectedUsers connectedUsers,
                                @Annotations.UsersWithAccount UsersWithAccount accounts,
                                @Annotations.SocialLogin SocialLogin socialLogin) {
        this.banList = banList;
        this.connectedUsers = connectedUsers;
        this.accounts = accounts;
        this.socialLogin = socialLogin;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        if (banList.contains(exchange.getHostName()))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.BANNED);

        String nickname = params.get(Consts.GeneralKeys.NICKNAME);
        if (nickname == null)
            throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);
        if (!Pattern.matches(Consts.VALID_NAME_PATTERN, nickname))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.INVALID_NICK);
        if (connectedUsers.hasUser(nickname) || accounts.hasNickname(nickname))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.NICK_IN_USE);

        UserAccount account = null;
        Consts.AuthType type = Consts.AuthType.parse(params.get(Consts.GeneralKeys.AUTH_TYPE));
        switch (type) {
            case PASSWORD:
                String email = params.get(Consts.UserData.EMAIL);
                if (email == null || email.isEmpty())
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

                if (accounts.hasEmail(email))
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.EMAIL_IN_USE);

                String password = params.get(Consts.AuthType.PASSWORD);
                if (password == null || password.isEmpty())
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

                account = accounts.registerWithPassword(nickname, email, password);
                break;
            case GOOGLE:
                GoogleIdToken.Payload googleToken = socialLogin.verifyGoogle(params.get(Consts.AuthType.GOOGLE));
                if (googleToken == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.GOOGLE_INVALID_TOKEN);

                if (accounts.hasEmail(googleToken.getEmail()))
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.EMAIL_IN_USE);

                account = accounts.registerWithGoogle(nickname, googleToken);
                break;
            case FACEBOOK:
                FacebookToken facebookToken = socialLogin.verifyFacebook(params.get(Consts.AuthType.FACEBOOK));
                if (facebookToken == null)
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.FACEBOOK_INVALID_TOKEN);

                FacebookProfileInfo facebookInfo = socialLogin.infoFacebook(facebookToken.userId);
                if (accounts.hasEmail(facebookInfo.email))
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.EMAIL_IN_USE);

                account = accounts.registerWithFacebook(nickname, facebookToken, facebookInfo);
                break;
            case GITHUB:
                String githubToken = params.get(Consts.AuthType.GITHUB);
                if (githubToken == null)
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.GITHUB_INVALID_TOKEN);

                GithubProfileInfo githubInfo = socialLogin.infoGithub(githubToken);
                if (accounts.hasEmail(githubInfo.email))
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.EMAIL_IN_USE);

                account = accounts.registerWithGithub(nickname, githubInfo);
                break;
            case TWITTER:
                String twitterTokens = params.get(Consts.AuthType.TWITTER);
                if (twitterTokens == null)
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.TWITTER_INVALID_TOKEN);

                TwitterProfileInfo twitterInfo = socialLogin.infoTwitter(twitterTokens);
                if (accounts.hasEmail(twitterInfo.email))
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.EMAIL_IN_USE);

                account = accounts.registerWithTwitter(nickname, twitterInfo);
                break;
        }

        if (account == null) throw new UnsupportedOperationException();
        return account.toJson();
    }
}
