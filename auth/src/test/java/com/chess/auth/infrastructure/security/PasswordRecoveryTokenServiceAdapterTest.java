package com.chess.auth.infrastructure.security;

import com.chess.auth.domain.model.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordRecoveryTokenServiceAdapterTest {

    private final PasswordRecoveryTokenServiceAdapter tokenService = new PasswordRecoveryTokenServiceAdapter();

    @Test
    @DisplayName("Should generate, validate, and invalidate token")
    void shouldHandleTokenLifecycle() {
        Email email = new Email("token@chess.com");

        String token = tokenService.generateToken(email);
        assertNotNull(token);

        assertTrue(tokenService.validateToken(token, email));
        assertFalse(tokenService.validateToken(token, new Email("other@chess.com")));
        assertFalse(tokenService.validateToken("invalid-token", email));
        assertFalse(tokenService.validateToken(null, email));
        assertFalse(tokenService.validateToken(token, null));

        tokenService.invalidateToken(token);
        assertFalse(tokenService.validateToken(token, email));
        
        // Invalidate null safe check
        assertDoesNotThrow(() -> tokenService.invalidateToken(null));
    }
}
