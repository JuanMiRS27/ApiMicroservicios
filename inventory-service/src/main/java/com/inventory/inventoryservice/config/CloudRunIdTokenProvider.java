package com.inventory.inventoryservice.config;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class CloudRunIdTokenProvider {

    private final Map<String, CachedToken> cache = new ConcurrentHashMap<>();

    public String getToken(String audience) {
        CachedToken cachedToken = cache.get(audience);
        if (cachedToken != null && cachedToken.expiresAt().isAfter(Instant.now().plusSeconds(300))) {
            return cachedToken.value();
        }

        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            if (!(credentials instanceof IdTokenProvider idTokenProvider)) {
                throw new IllegalStateException("Las credenciales actuales no soportan ID tokens.");
            }

            IdTokenCredentials tokenCredentials = IdTokenCredentials.newBuilder()
                    .setIdTokenProvider(idTokenProvider)
                    .setTargetAudience(audience)
                    .build();
            AccessToken token = tokenCredentials.refreshAccessToken();
            Instant expiresAt = token.getExpirationTime() == null
                    ? Instant.now().plusSeconds(3600)
                    : token.getExpirationTime().toInstant();
            cache.put(audience, new CachedToken(token.getTokenValue(), expiresAt));
            return token.getTokenValue();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar un ID token para Cloud Run.", ex);
        }
    }

    private record CachedToken(String value, Instant expiresAt) {
    }
}
