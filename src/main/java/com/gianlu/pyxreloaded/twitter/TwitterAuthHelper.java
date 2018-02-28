package com.gianlu.pyxreloaded.twitter;

import com.gianlu.pyxreloaded.singletons.Preferences;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class TwitterAuthHelper {
    private final OAuth10aService service;
    private final JsonParser parser = new JsonParser();

    public TwitterAuthHelper(Preferences preferences) {
        service = new ServiceBuilder(preferences.getString("twitterAppId", ""))
                .apiSecret(preferences.getString("twitterAppSecret", ""))
                .callback(preferences.getString("twitterCallback", ""))
                .build(TwitterApi.Authenticate.instance());
    }

    public OAuth1RequestToken requestToken() throws IOException, ExecutionException, InterruptedException {
        return service.getRequestToken();
    }

    public String authorizationUrl(OAuth1RequestToken token) {
        return service.getAuthorizationUrl(token);
    }

    public OAuth1AccessToken accessToken(OAuth1RequestToken token, String verifier) throws InterruptedException, ExecutionException, IOException {
        return service.getAccessToken(token, verifier);
    }

    public TwitterProfileInfo info(OAuth1AccessToken token) throws InterruptedException, ExecutionException, IOException {
        OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.twitter.com/1.1/account/verify_credentials.json");
        service.signRequest(token, request);
        Response response = service.execute(request);
        return new TwitterProfileInfo(parser.parse(response.getBody()).getAsJsonObject());
    }
}
