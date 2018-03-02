package com.gianlu.pyxreloaded.facebook;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FacebookProfileInfo {
    public final String email;
    public final String pictureUrl;

    FacebookProfileInfo(JsonObject obj) throws NullPointerException, FacebookEmailNotVerifiedException {
        JsonElement email = obj.get("email");
        if (email == null || email.isJsonNull()) throw new FacebookEmailNotVerifiedException();
        else this.email = email.getAsString();

        JsonObject picture = obj.getAsJsonObject("picture").getAsJsonObject("data");
        this.pictureUrl = picture.get("url").getAsString();
    }
}
