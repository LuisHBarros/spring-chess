package com.chess.auth.domain.model;

import com.chess.auth.domain.exception.InvalidPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordTest {

    @Test
    @DisplayName("Should create raw Password when valid")
    void shouldCreateRawPassword() {
        Password password = Password.fromRaw("StrongPassword123!");
        assertEquals("StrongPassword123!", password.getValue());
        assertFalse(password.isHashed());
    }

    @Test
    @DisplayName("Should create hashed Password")
    void shouldCreateHashedPassword() {
        Password password = Password.fromHash("$2a$10$e8p2H1g8x7.Zp1");
        assertEquals("$2a$10$e8p2H1g8x7.Zp1", password.getValue());
        assertTrue(password.isHashed());
    }

    @Test
    @DisplayName("Should throw InvalidPasswordException for short raw passwords")
    void shouldThrowForShortPassword() {
        assertThrows(InvalidPasswordException.class, () -> Password.fromRaw("short"));
    }

    @Test
    @DisplayName("Should throw InvalidPasswordException for null or empty password")
    void shouldThrowForNullOrEmptyPassword() {
        assertThrows(InvalidPasswordException.class, () -> Password.fromRaw(null));
        assertThrows(InvalidPasswordException.class, () -> Password.fromRaw("   "));
        assertThrows(InvalidPasswordException.class, () -> Password.fromHash(null));
        assertThrows(InvalidPasswordException.class, () -> Password.fromHash(""));
    }

    @Test
    @DisplayName("Should throw InvalidPasswordException for weak raw passwords")
    void shouldThrowForWeakPasswords() {
        assertThrows(InvalidPasswordException.class, () -> Password.fromRaw("alllowercase1!")); // no uppercase
        assertThrows(InvalidPasswordException.class, () -> Password.fromRaw("AllLowercase!")); // no digit
        assertThrows(InvalidPasswordException.class, () -> Password.fromRaw("AllLowercase1")); // no special
    }

    @Test
    @DisplayName("Should obscure password value in toString()")
    void shouldObscureToString() {
        Password raw = Password.fromRaw("SecretPass123!");
        Password hash = Password.fromHash("someHashValue");

        assertFalse(raw.toString().contains("SecretPass123!"));
        assertFalse(hash.toString().contains("someHashValue"));
    }
}
