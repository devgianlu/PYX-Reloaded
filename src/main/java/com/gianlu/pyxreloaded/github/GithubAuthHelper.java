package com.gianlu.pyxreloaded.github;

import com.gianlu.pyxreloaded.singletons.Preferences;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class GithubAuthHelper {
    private static final String ACCESS_TOKEN = "https://github.com/login/oauth/access_token";
    private static final String USER = "https://api.github.com/user";
    private final String appId;
    private final String appSecret;
    private final HttpClient client;
    private final JsonParser parser = new JsonParser();

    public GithubAuthHelper(Preferences preferences) {
        this.client = HttpClients.createDefault();
        this.appId = preferences.getString("githubAppId", "");
        this.appSecret = preferences.getString("githubAppSecret", "");
    }

    public GithubProfileInfo info(@NotNull String accessToken) throws IOException {
        HttpGet get = new HttpGet(USER);
        get.addHeader("Authorization", "token " + accessToken);

        try {
            HttpResponse resp = client.execute(get);

            HttpEntity entity = resp.getEntity();
            if (entity == null) throw new IOException(new NullPointerException("HttpEntity is null"));

            JsonElement element = parser.parse(new InputStreamReader(entity.getContent()));
            if (!element.isJsonObject()) throw new IOException("Response is not of type JsonObject");

            return new GithubProfileInfo(element.getAsJsonObject());
        } catch (JsonParseException | NullPointerException ex) {
            throw new IOException(ex);
        } finally {
            get.releaseConnection();
        }
    }

    public String exchangeCode(@NotNull String code) throws IOException, GithubException {
        HttpPost post = new HttpPost(ACCESS_TOKEN);
        post.addHeader("Accept", "application/json");
        UrlEncodedFormEntity body = new UrlEncodedFormEntity(Arrays.asList(
                new BasicNameValuePair("code", code),
                new BasicNameValuePair("client_id", appId),
                new BasicNameValuePair("client_secret", appSecret)));
        post.setEntity(body);

        try {
            HttpResponse resp = client.execute(post);

            HttpEntity entity = resp.getEntity();
            if (entity == null) throw new IOException(new NullPointerException("HttpEntity is null"));

            JsonElement element = parser.parse(new InputStreamReader(entity.getContent()));
            if (!element.isJsonObject()) throw new IOException("Response is not of type JsonObject");

            JsonObject obj = element.getAsJsonObject();
            if (obj.has("error")) {
                throw new GithubException(obj);
            } else {
                String scopes = obj.get("scope").getAsString();
                if (!scopes.contains("read:user")) throw GithubException.invalidScopes();
                return obj.get("access_token").getAsString();
            }
        } catch (JsonParseException | NullPointerException ex) {
            throw new IOException(ex);
        } finally {
            post.releaseConnection();
        }
    }
}
