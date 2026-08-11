package com.chess.auth.infrastructure.security;

import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.port.PasswordRecoveryTokenService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PasswordRecoveryTokenServiceAdapter implements PasswordRecoveryTokenService {

    private static final long TOKEN_VALIDITY_SECONDS = 900; // 15 minutes

    private final Map<String, TokenMetadata> tokenStore = new ConcurrentHashMap<>();

    private static class TokenMetadata {
        private final Email email;
        private final Instant expiresAt;

        public TokenMetadata(Email email, Instant expiresAt) {
            this.email = email;
            this.expiresAt = expiresAt;
        }

        public Email getEmail() {
            return email;
        }

        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    @Override
    public String generateToken(Email email) {
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(TOKEN_VALIDITY_SECONDS);
        tokenStore.put(token, new TokenMetadata(email, expiresAt));
        return token;
    }

    @Override
    public boolean validateToken(String token, Email email) {
        if (token == null || email == null) {
            return false;
        }
        TokenMetadata metadata = tokenStore.get(token);
        if (metadata == null) {
            return false;
        }
        if (metadata.isExpired()) {
            tokenStore.remove(token);
            return false;
        }
        return metadata.getEmail().equals(email);
    }

    @Override
    public void invalidateToken(String token) {
        if (token != null) {
            tokenStore.remove(token);
        }
    }
}
