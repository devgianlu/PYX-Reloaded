package com.gianlu.pyxreloaded.google;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.singletons.Preferences;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.testing.json.MockJsonFactory;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleTokenVerifierService {
    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifierService(Preferences preferences) {
        verifier = new GoogleIdTokenVerifier.Builder(new ApacheHttpTransport(), new MockJsonFactory())
                .setAudience(Collections.singletonList(preferences.getString("googleClientId", "")))
                .build();
    }

    @Nullable
    public GoogleIdToken.Payload verify(String tokenStr) throws BaseCahHandler.CahException {
        if (tokenStr == null) return null;

        try {
            GoogleIdToken token = verifier.verify(tokenStr);
            return token == null ? null : token.getPayload();
        } catch (GeneralSecurityException | IOException ex) {
            throw new BaseCahHandler.CahException(Consts.ErrorCode.GOOGLE_ERROR, ex);
        }
    }
}
