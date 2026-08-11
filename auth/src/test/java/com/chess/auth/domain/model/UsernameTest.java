package com.chess.auth.domain.model;

import com.chess.auth.domain.exception.InvalidUsernameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UsernameTest {

    @Test
    @DisplayName("Should create valid Username")
    void shouldCreateValidUsername() {
        Username username = new Username("chess_master");
        assertEquals("chess_master", username.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "a", "", "   ", "user@name", "user-name", "too_long_username_exceeding_thirty_characters"})
    @DisplayName("Should throw InvalidUsernameException for invalid usernames")
    void shouldThrowExceptionForInvalidUsernames(String invalid) {
        assertThrows(InvalidUsernameException.class, () -> new Username(invalid));
    }

    @Test
    @DisplayName("Should throw InvalidUsernameException for null username")
    void shouldThrowExceptionForNullUsername() {
        assertThrows(InvalidUsernameException.class, () -> new Username(null));
    }

    @Test
    @DisplayName("Should satisfy equality based on value")
    void shouldSatisfyEquality() {
        Username u1 = new Username("player1");
        Username u2 = new Username("player1");
        assertEquals(u1, u2);
        assertEquals(u1.hashCode(), u2.hashCode());
    }
}
