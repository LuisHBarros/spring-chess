package com.chess.auth.infrastructure.security;

import com.chess.auth.domain.model.Password;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BCryptPasswordEncoderAdapterTest {

    private final BCryptPasswordEncoderAdapter encoder = new BCryptPasswordEncoderAdapter();

    @Test
    @DisplayName("Should encode raw password into BCrypt hash and match correctly")
    void shouldEncodeAndMatchPassword() {
        Password raw = Password.fromRaw("MySecurePassword123!");

        Password hashed = encoder.encode(raw);

        assertTrue(hashed.isHashed());
        assertNotEquals("MySecurePassword123!", hashed.getValue());
        assertTrue(encoder.matches(raw, hashed));
        assertFalse(encoder.matches(Password.fromRaw("WrongPassword123!"), hashed));
    }
}
