package com.gianlu.pyxreloaded.socials.github;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class GithubEmails extends ArrayList<GithubEmails.Email> {
    GithubEmails(JsonArray array) {
        for (JsonElement element : array) add(new Email(element.getAsJsonObject()));
    }

    public boolean isPrimaryEmailVerified() {
        for (Email email : this) {
            if (email.primary && email.verified)
                return true;
        }

        return false;
    }

    public static class Email {
        public final boolean verified;
        public final boolean primary;
        public final String email;

        Email(JsonObject obj) {
            verified = obj.get("verified").getAsBoolean();
            primary = obj.get("primary").getAsBoolean();
            email = obj.get("email").getAsString();
        }
    }
}
