package com.gianlu.pyxreloaded.github;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class GithubException extends Exception {
    private GithubException(String message) {
        super(message);
    }

    @NotNull
    public static GithubException invalidScopes() {
        return new GithubException("Invalid scopes!");
    }

    @NotNull
    public static GithubException fromMessage(JsonObject obj) {
        return new GithubException(obj.get("message").getAsString());
    }

    @NotNull
    public static GithubException fromError(JsonObject obj) {
        return new GithubException(obj.get("error").getAsString());
    }
}
