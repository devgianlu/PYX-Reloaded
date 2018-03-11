package com.gianlu.pyxreloaded.socials.facebook;

import com.gianlu.pyxreloaded.singletons.Preferences;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;

public class FacebookAuthHelper {
    private static final String DEBUG_TOKEN = "https://graph.facebook.com/debug_token";
    private static final String GRAPH = "https://graph.facebook.com/";
    private final CloseableHttpClient client;
    private final String appToken;
    private final JsonParser parser = new JsonParser();
    private final String appId;

    private FacebookAuthHelper(@NotNull String appId, @NotNull String appSecret) {
        this.client = HttpClients.createDefault();
        this.appId = appId;
        this.appToken = appId + "%7C" + appSecret; // URL encoded
    }

    @Nullable
    public static FacebookAuthHelper instantiate(Preferences preferences) {
        String appId = preferences.getString("socials/facebookAppId", null);
        if (appId == null || appId.isEmpty()) return null;

        String appSecret = preferences.getString("socials/facebookAppSecret", null);
        if (appSecret == null || appSecret.isEmpty()) return null;

        return new FacebookAuthHelper(appId, appSecret);
    }

    @NotNull
    public String appId() {
        return appId;
    }

    @Nullable
    public FacebookToken verify(@NotNull String accessToken) throws IOException, FacebookOAuthException {
        HttpGet get = new HttpGet(DEBUG_TOKEN + "?input_token=" + accessToken + "&access_token=" + appToken);
        try {
            HttpResponse resp = client.execute(get);

            HttpEntity entity = resp.getEntity();
            if (entity == null) throw new IOException(new NullPointerException("HttpEntity is null"));

            JsonElement element = parser.parse(new InputStreamReader(entity.getContent()));
            if (!element.isJsonObject()) throw new IOException("Response is not of type JsonObject");

            JsonObject obj = element.getAsJsonObject();
            if (obj.has("data")) {
                FacebookToken token = new FacebookToken(obj.getAsJsonObject("data"));
                if (token.valid && appId.equals(token.appId)) return token;
                else return null;
            } else if (obj.has("error")) {
                throw new FacebookOAuthException(obj.getAsJsonObject("error"));
            } else {
                throw new IOException("What is that? " + obj);
            }
        } catch (JsonParseException | NullPointerException ex) {
            throw new IOException(ex);
        } finally {
            get.releaseConnection();
        }
    }

    @NotNull
    public FacebookProfileInfo info(String userId) throws FacebookOAuthException, IOException, FacebookEmailNotVerifiedException {
        HttpGet get = new HttpGet(GRAPH + userId + "/?fields=picture.type(large),email&access_token=" + appToken);
        try {
            HttpResponse resp = client.execute(get);

            HttpEntity entity = resp.getEntity();
            if (entity == null) throw new IOException(new NullPointerException("HttpEntity is null"));

            JsonElement element = parser.parse(new InputStreamReader(entity.getContent()));
            if (!element.isJsonObject()) throw new IOException("Response is not of type JsonObject");

            JsonObject obj = element.getAsJsonObject();
            if (obj.has("error")) throw new FacebookOAuthException(obj.getAsJsonObject("error"));
            else return new FacebookProfileInfo(obj);
        } catch (JsonParseException | NullPointerException ex) {
            throw new IOException(ex);
        } finally {
            get.releaseConnection();
        }
    }

    public void close() throws IOException {
        client.close();
    }
}
