package com.gianlu.pyxreloaded.facebook;

import com.google.gson.JsonObject;

public class FacebookOAuthException extends Exception {
    FacebookOAuthException(JsonObject error) {
        super(error.get("message").getAsString());
    }
}
