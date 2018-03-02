package com.gianlu.pyxreloaded.twitter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TwitterProfileInfo {
    public final String id;
    public final String avatarUrl;
    public final String email;

    TwitterProfileInfo(JsonObject obj) throws TwitterEmailNotVerifiedException {
        this.id = obj.get("id_str").getAsString();
        this.avatarUrl = obj.get("profile_image_url_https").getAsString()
                .replaceAll("(_(?:normal|bigger|mini))", "");

        JsonElement email = obj.get("email");
        if (email == null || email.isJsonNull()) throw new TwitterEmailNotVerifiedException();
        else this.email = email.getAsString();
    }
}
