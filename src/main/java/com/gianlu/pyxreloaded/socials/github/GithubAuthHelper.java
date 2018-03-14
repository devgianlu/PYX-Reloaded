package com.gianlu.pyxreloaded.socials.github;

import com.gianlu.pyxreloaded.singletons.Preferences;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class GithubAuthHelper {
    private static final String ACCESS_TOKEN = "https://github.com/login/oauth/access_token";
    private static final String USER = "https://api.github.com/user";
    private static final String EMAILS = "https://api.github.com/user/emails";
    private final String appId;
    private final String appSecret;
    private final CloseableHttpClient client;
    private final JsonParser parser = new JsonParser();

    private GithubAuthHelper(@NotNull String appId, @NotNull String appSecret) {
        this.client = HttpClients.createDefault();
        this.appId = appId;
        this.appSecret = appSecret;
    }

    @Nullable
    public static GithubAuthHelper instantiate(Preferences preferences) {
        String appId = preferences.getString("socials/githubAppId", null);
        if (appId == null || appId.isEmpty()) return null;

        String appSecret = preferences.getString("socials/githubAppSecret", null);
        if (appSecret == null || appSecret.isEmpty()) return null;

        return new GithubAuthHelper(appId, appSecret);
    }

    public GithubProfileInfo info(@NotNull String accessToken, @NotNull GithubEmails emails) throws IOException, GithubException {
        HttpGet get = new HttpGet(USER);
        get.addHeader("Authorization", "token " + accessToken);

        try {
            HttpResponse resp = client.execute(get);

            HttpEntity entity = resp.getEntity();
            if (entity == null) throw new IOException(new NullPointerException("HttpEntity is null"));

            JsonElement element = parser.parse(new InputStreamReader(entity.getContent()));
            if (!element.isJsonObject()) throw new IOException("Response is not of type JsonObject");

            JsonObject obj = new JsonObject();
            if (obj.has("message")) throw GithubException.fromMessage(obj);
            else return new GithubProfileInfo(element.getAsJsonObject(), emails);
        } catch (JsonParseException | NullPointerException ex) {
            throw new IOException(ex);
        } finally {
            get.releaseConnection();
        }
    }

    public GithubEmails emails(@NotNull String accessToken) throws IOException, GithubException {
        HttpGet get = new HttpGet(EMAILS);
        get.addHeader("Authorization", "token " + accessToken);

        try {
            HttpResponse resp = client.execute(get);

            HttpEntity entity = resp.getEntity();
            if (entity == null) throw new IOException(new NullPointerException("HttpEntity is null"));

            JsonElement element = parser.parse(new InputStreamReader(entity.getContent()));
            if (element.isJsonArray()) {
                return new GithubEmails(element.getAsJsonArray());
            } else if (element.isJsonObject()) {
                JsonObject obj = new JsonObject();
                if (obj.has("message")) throw GithubException.fromMessage(obj);
                throw new IOException("I am confused. " + element);
            } else {
                throw new IOException("What is that? " + element);
            }
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
                throw GithubException.fromError(obj);
            } else {
                String scopes = obj.get("scope").getAsString();
                if (!scopes.contains("read:user") || !scopes.contains("user:email"))
                    throw GithubException.invalidScopes();

                return obj.get("access_token").getAsString();
            }
        } catch (JsonParseException | NullPointerException ex) {
            throw new IOException(ex);
        } finally {
            post.releaseConnection();
        }
    }

    @NotNull
    public String appId() {
        return appId;
    }

    public void close() throws IOException {
        client.close();
    }
}
