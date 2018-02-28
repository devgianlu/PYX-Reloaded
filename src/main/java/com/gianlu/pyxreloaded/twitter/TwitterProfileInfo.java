package com.gianlu.pyxreloaded.twitter;

import com.google.gson.JsonObject;

public class TwitterProfileInfo {
    public final String id;
    public final String avatarUrl;

    TwitterProfileInfo(JsonObject obj) {
        id = obj.get("id_str").getAsString();
        avatarUrl = obj.get("profile_image_url_https").getAsString();
    }
}
