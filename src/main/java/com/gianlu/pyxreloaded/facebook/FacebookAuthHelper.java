package com.gianlu.pyxreloaded.facebook;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;

public class FacebookAuthHelper {
    private static final String DEBUG_TOKEN = "https://graph.facebook.com/debug_token";
    private static final String GRAPH = "https://graph.facebook.com/";
    private final HttpClient client;
    private final String appToken;
    private final JsonParser parser = new JsonParser();
    private final String appId;

    public FacebookAuthHelper(String appId, String appSecret) {
        this.client = HttpClients.createDefault();
        this.appToken = appId + "%7C" + appSecret; // URL encoded
        this.appId = appId;
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
    public FacebookProfileInfo info(String userId) throws FacebookOAuthException, IOException {
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
}
