package com.gianlu.pyxreloaded.socials.github;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class GithubProfileInfo {
    public final String id;
    public final String email;
    public final String avatarUrl;
    public final GithubEmails emails;

    GithubProfileInfo(JsonObject obj, @NotNull GithubEmails emails) {
        this.id = obj.get("id").getAsString();
        this.email = obj.get("email").getAsString();
        this.avatarUrl = obj.get("avatar_url").getAsString();
        this.emails = emails;
    }
}
