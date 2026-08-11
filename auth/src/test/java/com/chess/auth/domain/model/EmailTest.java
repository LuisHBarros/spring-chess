package com.chess.auth.domain.model;

import com.chess.auth.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    @DisplayName("Should create valid Email and normalize to lowercase")
    void shouldCreateValidEmail() {
        Email email = new Email("User.Test@Example.COM");
        assertEquals("user.test@example.com", email.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "@domain.com", "user@", "user@domain", "", "   "})
    @DisplayName("Should throw InvalidEmailException for invalid email formats")
    void shouldThrowExceptionForInvalidEmails(String invalid) {
        assertThrows(InvalidEmailException.class, () -> new Email(invalid));
    }

    @Test
    @DisplayName("Should throw InvalidEmailException when email is null")
    void shouldThrowExceptionWhenNull() {
        assertThrows(InvalidEmailException.class, () -> new Email(null));
    }

    @Test
    @DisplayName("Should satisfy equality based on value")
    void shouldSatisfyEquality() {
        Email e1 = new Email("test@example.com");
        Email e2 = new Email("TEST@EXAMPLE.COM");
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }
}
