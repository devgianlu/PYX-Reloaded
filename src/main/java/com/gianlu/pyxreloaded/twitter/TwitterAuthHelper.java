package com.gianlu.pyxreloaded.twitter;

import com.gianlu.pyxreloaded.Utils;
import com.gianlu.pyxreloaded.singletons.Preferences;
import com.google.gson.JsonParser;
import com.hunorkovacs.koauth.domain.KoauthRequest;
import com.hunorkovacs.koauth.service.consumer.RequestWithInfo;
import com.hunorkovacs.koauthsync.service.consumer.ConsumerService;
import com.hunorkovacs.koauthsync.service.consumer.DefaultConsumerService;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;
import scala.Option;
import scala.concurrent.ExecutionContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwitterAuthHelper {
    private static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
    private static final String AUTH_URL = "https://api.twitter.com/oauth/authenticate";
    private static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";
    private final JsonParser parser = new JsonParser();
    private final ConsumerService consumer;
    private final String appId;
    private final String appSecret;
    private final String callback;
    private final HttpClient client;
    private final TokensStore store;

    public TwitterAuthHelper(Preferences preferences) {
        appId = preferences.getString("twitterAppId", "");
        appSecret = preferences.getString("twitterAppSecret", "");
        callback = preferences.getString("twitterCallback", "");
        consumer = new DefaultConsumerService(ExecutionContext.Implicits$.MODULE$.global());
        client = HttpClients.createDefault();
        store = new TokensStore();
    }

    private HttpPost requestTokenRequest() {
        HttpPost post = new HttpPost(REQUEST_TOKEN_URL);
        RequestWithInfo info = consumer.createRequestTokenRequest(KoauthRequest.apply("POST",
                REQUEST_TOKEN_URL, Option.empty()), appId, appSecret, callback);
        post.setHeader("Authorization", info.header());
        return post;
    }

    private HttpPost accessTokenRequest(String token, String verifier) {
        HttpPost post = new HttpPost(ACCESS_TOKEN_URL);
        RequestWithInfo info = consumer.createAccessTokenRequest(KoauthRequest.apply("POST",
                ACCESS_TOKEN_URL, Option.empty()), appId, appSecret, token, store.secret(token), verifier);

        post.setHeader("Authorization", info.header());
        return post;
    }

    public String authorizationUrl() throws IOException {
        HttpPost post = requestTokenRequest();

        try {
            HttpResponse resp = client.execute(post);
            List<NameValuePair> body = URLEncodedUtils.parse(EntityUtils.toString(resp.getEntity()), Charset.forName("UTF-8"));

            if (!Boolean.parseBoolean(Utils.get(body, "oauth_callback_confirmed")))
                throw new IOException("Callback not confirmed!");

            String tokenSecret = Utils.get(body, "oauth_token_secret");
            String token = Utils.get(body, "oauth_token");
            store.add(token, tokenSecret);

            return AUTH_URL + "?force_login=false&oauth_token=" + token;
        } finally {
            post.releaseConnection();
        }
    }

    public String accessToken(String token, String verifier) throws IOException {
        HttpPost post = accessTokenRequest(token, verifier);

        try {
            HttpResponse resp = client.execute(post);
            List<NameValuePair> body = URLEncodedUtils.parse(EntityUtils.toString(resp.getEntity()), Charset.forName("UTF-8"));
            return Utils.get(body, "oauth_token");
        } finally {
            post.releaseConnection();
        }
    }

    private static class TokensStore {
        private final Map<String, String> map = new HashMap<>();

        synchronized void add(String token, String tokenSecret) {
            map.put(token, tokenSecret);
        }

        @Nullable
        synchronized String secret(String token) {
            return map.remove(token);
        }
    }
}
