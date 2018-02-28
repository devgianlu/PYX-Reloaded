package com.gianlu.pyxreloaded.twitter;

import com.gianlu.pyxreloaded.singletons.Preferences;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TwitterAuthHelper {
    private final OAuth10aService service;
    private final TokensTemporaryStore store;
    private final JsonParser parser = new JsonParser();

    public TwitterAuthHelper(Preferences preferences) {
        store = new TokensTemporaryStore();
        service = new ServiceBuilder(preferences.getString("twitterAppId", ""))
                .apiSecret(preferences.getString("twitterAppSecret", ""))
                .callback(preferences.getString("twitterCallback", ""))
                .build(TwitterApi.Authenticate.instance());
    }

    public String authorizationUrl() throws IOException, ExecutionException, InterruptedException {
        OAuth1RequestToken requestToken = service.getRequestToken();
        store.add(requestToken);
        return service.getAuthorizationUrl(requestToken) + "&force_login=false";
    }

    public OAuth1AccessToken accessToken(String token, String verifier) throws InterruptedException, ExecutionException, IOException {
        String secret = store.secret(token);
        if (secret == null) throw new IOException(new NullPointerException("Token secret is null!"));

        return service.getAccessToken(new OAuth1RequestToken(token, secret), verifier);
    }

    public TwitterProfileInfo info(OAuth1AccessToken token) throws InterruptedException, ExecutionException, IOException {
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.twitter.com/1.1/account/verify_credentials.json");
        service.signRequest(token, request);
        Response response = service.execute(request);
        return new TwitterProfileInfo(parser.parse(response.getBody()).getAsJsonObject());
    }

    private static class TokensTemporaryStore {
        private final Map<String, String> map = new HashMap<>();

        synchronized void add(@NotNull OAuth1RequestToken requestToken) {
            map.put(requestToken.getToken(), requestToken.getTokenSecret());
        }

        @Nullable
        synchronized String secret(String token) {
            return map.remove(token);
        }
    }
}
