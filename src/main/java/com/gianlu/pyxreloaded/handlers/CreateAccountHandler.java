package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.data.accounts.UserAccount;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.BaseJsonHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.*;
import com.gianlu.pyxreloaded.socials.facebook.FacebookProfileInfo;
import com.gianlu.pyxreloaded.socials.facebook.FacebookToken;
import com.gianlu.pyxreloaded.socials.github.GithubProfileInfo;
import com.gianlu.pyxreloaded.socials.twitter.TwitterProfileInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.undertow.server.HttpServerExchange;

import java.text.ParseException;
import java.util.regex.Pattern;

public class CreateAccountHandler extends BaseHandler {
    public static final String OP = Consts.Operation.CREATE_ACCOUNT.toString();
    private final BanList banList;
    private final ConnectedUsers connectedUsers;
    private final UsersWithAccount accounts;
    private final SocialLogin socialLogin;
    private final Emails emails;

    public CreateAccountHandler(@Annotations.BanList BanList banList,
                                @Annotations.ConnectedUsers ConnectedUsers connectedUsers,
                                @Annotations.UsersWithAccount UsersWithAccount accounts,
                                @Annotations.SocialLogin SocialLogin socialLogin,
                                @Annotations.Emails Emails emails) {
        this.banList = banList;
        this.connectedUsers = connectedUsers;
        this.accounts = accounts;
        this.socialLogin = socialLogin;
        this.emails = emails;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        if (banList.contains(exchange.getHostName()))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.BANNED);

        String nickname = params.getStringNotNull(Consts.GeneralKeys.NICKNAME);
        if (!Pattern.matches(Consts.VALID_NAME_PATTERN, nickname))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.INVALID_NICK);
        if (connectedUsers.hasUser(nickname) || accounts.hasNickname(nickname))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.NICK_IN_USE);

        UserAccount account;
        Consts.AuthType type;
        try {
            type = Consts.AuthType.parse(params.getStringNotNull(Consts.GeneralKeys.AUTH_TYPE));
        } catch (ParseException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST, ex);
        }

        switch (type) {
            case PASSWORD:
                if (!emails.enabled()) throw new BaseCahHandler.CahException(Consts.ErrorCode.UNSUPPORTED_AUTH_TYPE);

                String email = params.getStringNotNull(Consts.UserData.EMAIL);
                if (email.isEmpty()) throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

                if (accounts.hasEmail(email)) throw new BaseCahHandler.CahException(Consts.ErrorCode.EMAIL_IN_USE);

                String password = params.getStringNotNull(Consts.AuthType.PASSWORD);
                if (password.isEmpty()) throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

                account = accounts.registerWithPassword(nickname, email, password);
                emails.sendEmailVerification(account);
                break;
            case GOOGLE:
                if (!socialLogin.googleEnabled())
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.UNSUPPORTED_AUTH_TYPE);

                GoogleIdToken.Payload googleToken = socialLogin.verifyGoogle(params.getStringNotNull(Consts.AuthType.GOOGLE));
                if (googleToken == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.GOOGLE_INVALID_TOKEN);

                if (accounts.hasEmail(googleToken.getEmail()))
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.EMAIL_IN_USE);

                account = accounts.registerWithGoogle(nickname, googleToken);
                break;
            case FACEBOOK:
                if (!socialLogin.facebookEnabled())
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.UNSUPPORTED_AUTH_TYPE);

                FacebookToken facebookToken = socialLogin.verifyFacebook(params.getStringNotNull(Consts.AuthType.FACEBOOK));
                if (facebookToken == null)
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.FACEBOOK_INVALID_TOKEN);

                FacebookProfileInfo facebookInfo = socialLogin.infoFacebook(facebookToken.userId);
                if (accounts.hasEmail(facebookInfo.email))
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.EMAIL_IN_USE);

                account = accounts.registerWithFacebook(nickname, facebookToken, facebookInfo);
                break;
            case GITHUB:
                if (!socialLogin.githubEnabled())
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.UNSUPPORTED_AUTH_TYPE);

                String githubToken = params.getString(Consts.AuthType.GITHUB);
                if (githubToken == null)
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.GITHUB_INVALID_TOKEN);

                GithubProfileInfo githubInfo = socialLogin.infoGithub(githubToken);
                if (accounts.hasEmail(githubInfo.email))
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.EMAIL_IN_USE);

                account = accounts.registerWithGithub(nickname, githubInfo);
                break;
            case TWITTER:
                if (!socialLogin.twitterEnabled())
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.UNSUPPORTED_AUTH_TYPE);

                String twitterTokens = params.getString(Consts.AuthType.TWITTER);
                if (twitterTokens == null)
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.TWITTER_INVALID_TOKEN);

                TwitterProfileInfo twitterInfo = socialLogin.infoTwitter(twitterTokens);
                if (accounts.hasEmail(twitterInfo.email))
                    throw new BaseCahHandler.CahException(Consts.ErrorCode.EMAIL_IN_USE);

                account = accounts.registerWithTwitter(nickname, twitterInfo);
                break;
            default:
                throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);
        }

        return account.toJson();
    }
}
