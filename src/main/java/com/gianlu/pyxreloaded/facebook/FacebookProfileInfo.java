package com.gianlu.pyxreloaded.facebook;

import com.google.gson.JsonObject;

public class FacebookProfileInfo {
    public final String email;
    public final String pictureUrl;

    FacebookProfileInfo(JsonObject obj) throws NullPointerException {
        email = obj.get("email").getAsString();

        JsonObject picture = obj.getAsJsonObject("picture").getAsJsonObject("data");
        pictureUrl = picture.get("url").getAsString();
    }
}
