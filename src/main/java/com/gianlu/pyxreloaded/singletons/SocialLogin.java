package com.gianlu.pyxreloaded.singletons;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public final class SocialLogin {
    private final GoogleIdTokenVerifier googleVerifier;

    public SocialLogin(Preferences preferences) {
        googleVerifier = new GoogleIdTokenVerifier.Builder(new ApacheHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(preferences.getString("googleClientId", "")))
                .build();
    }

    @Nullable
    public GoogleIdToken.Payload verifyGoogle(String tokenStr) throws BaseCahHandler.CahException {
        if (tokenStr == null) return null;

        try {
            GoogleIdToken token = googleVerifier.verify(tokenStr);
            return token == null ? null : token.getPayload();
        } catch (GeneralSecurityException | IOException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.GOOGLE_ERROR, ex);
        }
    }
}
