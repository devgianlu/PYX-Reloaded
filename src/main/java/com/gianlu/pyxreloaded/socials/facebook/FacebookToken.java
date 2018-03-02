package com.gianlu.pyxreloaded.socials.facebook;

import com.google.gson.JsonObject;

public class FacebookToken {
    public final String appId;
    public final String userId;
    final boolean valid;

    FacebookToken(JsonObject obj) throws NullPointerException {
        appId = obj.get("app_id").getAsString();
        valid = obj.get("is_valid").getAsBoolean();
        userId = obj.get("user_id").getAsString();
    }
}
