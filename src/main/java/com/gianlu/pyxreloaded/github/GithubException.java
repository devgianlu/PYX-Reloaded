package com.gianlu.pyxreloaded.github;

import com.google.gson.JsonObject;

public class GithubException extends Exception {
    GithubException(JsonObject error) {
        super(error.get("error").getAsString());
    }
}
