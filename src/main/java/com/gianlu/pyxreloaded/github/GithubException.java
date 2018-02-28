package com.gianlu.pyxreloaded.github;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class GithubException extends Exception {
    GithubException(JsonObject error) {
        super(error.get("error").getAsString());
    }

    private GithubException(String message) {
        super(message);
    }

    @NotNull
    public static GithubException invalidScopes() {
        return new GithubException("Invalid scopes!");
    }
}
