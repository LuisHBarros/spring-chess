package com.chess.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthApplicationTest {

    @Test
    @DisplayName("Should instantiate AuthApplication class")
    void shouldInstantiateAuthApplication() {
        AuthApplication app = new AuthApplication();
        assertNotNull(app);
    }
}
