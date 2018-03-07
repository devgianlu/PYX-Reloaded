package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.Utils;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.socials.facebook.*;
import com.gianlu.pyxreloaded.socials.github.GithubAuthHelper;
import com.gianlu.pyxreloaded.socials.github.GithubException;
import com.gianlu.pyxreloaded.socials.github.GithubProfileInfo;
import com.gianlu.pyxreloaded.socials.twitter.TwitterAuthHelper;
import com.gianlu.pyxreloaded.socials.twitter.TwitterEmailNotVerifiedException;
import com.gianlu.pyxreloaded.socials.twitter.TwitterProfileInfo;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public final class SocialLogin {
    private final GithubAuthHelper githubHelper;
    private final TwitterAuthHelper twitterHelper;
    private final GoogleIdTokenVerifier googleHelper;
    private final FacebookAuthHelper facebookHelper;

    public SocialLogin(@Nullable GithubAuthHelper githubHelper, @Nullable TwitterAuthHelper twitterHelper, @Nullable FacebookAuthHelper facebookHelper, Preferences preferences) {
        this.githubHelper = githubHelper;
        this.twitterHelper = twitterHelper;
        this.facebookHelper = facebookHelper;
        this.googleHelper = instantiateGoogleHelper(preferences);
    }

    @Nullable
    private static GoogleIdTokenVerifier instantiateGoogleHelper(Preferences preferences) {
        String clientId = preferences.getString("socials/googleClientId", null);
        if (clientId == null || clientId.isEmpty()) return null;

        return new GoogleIdTokenVerifier.Builder(new ApacheHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    @Contract("null -> null")
    @Nullable
    public GoogleIdToken.Payload verifyGoogle(String tokenStr) throws BaseCahHandler.CahException {
        if (tokenStr == null) return null;

        try {
            GoogleIdToken token = googleHelper.verify(tokenStr);
            return token == null ? null : token.getPayload();
        } catch (GeneralSecurityException | IOException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.GOOGLE_ERROR, ex);
        }
    }

    @NotNull
    public FacebookProfileInfo infoFacebook(String userId) throws BaseCahHandler.CahException {
        try {
            return facebookHelper.info(userId);
        } catch (FacebookOAuthException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.FACEBOOK_INVALID_TOKEN, ex);
        } catch (IOException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.FACEBOOK_ERROR, ex);
        } catch (FacebookEmailNotVerifiedException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.FACEBOOK_EMAIL_NOT_VERIFIED, ex);
        }
    }

    @Contract("null -> null")
    @Nullable
    public FacebookToken verifyFacebook(String accessToken) throws BaseCahHandler.CahException {
        if (accessToken == null) return null;

        try {
            return facebookHelper.verify(accessToken);
        } catch (IOException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.FACEBOOK_ERROR, ex);
        } catch (FacebookOAuthException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.FACEBOOK_INVALID_TOKEN, ex);
        }
    }

    @NotNull
    public GithubProfileInfo infoGithub(@NotNull String accessToken) throws BaseCahHandler.CahException {
        try {
            return githubHelper.info(accessToken, githubHelper.emails(accessToken));
        } catch (IOException | GithubException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.GITHUB_ERROR, ex);
        }
    }

    @NotNull
    public TwitterProfileInfo infoTwitter(@NotNull String tokens) throws BaseCahHandler.CahException {
        try {
            List<NameValuePair> pairs = URLEncodedUtils.parse(tokens, Charset.forName("UTF-8"));

            String token = Utils.get(pairs, "oauth_token");
            if (token == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.TWITTER_INVALID_TOKEN);

            String tokenSecret = Utils.get(pairs, "oauth_token_secret");
            if (tokenSecret == null) throw new BaseCahHandler.CahException(Consts.ErrorCode.TWITTER_INVALID_TOKEN);

            return twitterHelper.info(new OAuth1AccessToken(token, tokenSecret, tokens));
        } catch (IOException | ExecutionException | InterruptedException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.TWITTER_ERROR, ex);
        } catch (TwitterEmailNotVerifiedException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.TWITTER_EMAIL_NOT_VERIFIED, ex);
        }
    }

    @Contract(pure = true)
    public boolean googleEnabled() {
        return googleHelper != null;
    }

    @Contract(pure = true)
    public boolean facebookEnabled() {
        return facebookHelper != null;
    }

    @Contract(pure = true)
    public boolean githubEnabled() {
        return githubHelper != null;
    }

    @Contract(pure = true)
    public boolean twitterEnabled() {
        return twitterHelper != null;
    }
}
