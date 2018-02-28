package com.gianlu.pyxreloaded.twitter;

import com.google.gson.JsonObject;

public class TwitterProfileInfo {
    public final String id;
    public final String avatarUrl;
    public final String email;

    TwitterProfileInfo(JsonObject obj) {
        id = obj.get("id_str").getAsString();
        email = obj.get("email").getAsString();
        avatarUrl = obj.get("profile_image_url_https").getAsString()
                .replaceAll("(_(?:normal|bigger|mini))", "");
    }
}
