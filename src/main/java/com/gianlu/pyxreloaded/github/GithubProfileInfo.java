package com.gianlu.pyxreloaded.github;

import com.google.gson.JsonObject;

public class GithubProfileInfo {
    public final String id;
    public final String email;

    GithubProfileInfo(JsonObject obj) {
        id = obj.get("id").getAsString();
        email = obj.get("email").getAsString();
    }
}
