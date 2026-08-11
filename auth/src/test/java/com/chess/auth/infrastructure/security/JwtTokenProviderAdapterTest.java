package com.chess.auth.infrastructure.security;

import com.chess.auth.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderAdapterTest {

    private JwtTokenProviderAdapter jwtTokenProvider;
    private User testUser;

    @BeforeEach
    void setUp() {
        String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        jwtTokenProvider = new JwtTokenProviderAdapter(secret, 3600000, 604800000);
        testUser = User.create(new Username("jwt_player"), new Email("player@jwt.com"), Password.fromHash("hashed_pass"));
    }

    @Test
    @DisplayName("Should generate valid JWT access and refresh tokens")
    void shouldGenerateValidTokens() {
        AuthToken tokenPair = jwtTokenProvider.generateTokens(testUser);

        assertNotNull(tokenPair);
        assertNotNull(tokenPair.getAccessToken());
        assertNotNull(tokenPair.getRefreshToken());
        assertEquals("Bearer", tokenPair.getTokenType());
        assertEquals(3600, tokenPair.getExpiresInSeconds());

        assertTrue(jwtTokenProvider.validateAccessToken(tokenPair.getAccessToken()));
        assertTrue(jwtTokenProvider.validateRefreshToken(tokenPair.getRefreshToken()));

        assertFalse(jwtTokenProvider.validateAccessToken(tokenPair.getRefreshToken()));
        assertFalse(jwtTokenProvider.validateRefreshToken(tokenPair.getAccessToken()));
    }

    @Test
    @DisplayName("Should extract userId and email from JWT tokens")
    void shouldExtractClaims() {
        AuthToken tokenPair = jwtTokenProvider.generateTokens(testUser);

        UserId extractedId = jwtTokenProvider.extractUserIdFromAccessToken(tokenPair.getAccessToken());
        assertEquals(testUser.getId(), extractedId);

        Email extractedEmail = jwtTokenProvider.extractEmailFromRefreshToken(tokenPair.getRefreshToken());
        assertEquals(testUser.getEmail(), extractedEmail);
    }

    @Test
    @DisplayName("Should return remaining expiration seconds")
    void shouldGetRemainingExpiration() {
        AuthToken tokenPair = jwtTokenProvider.generateTokens(testUser);
        long remaining = jwtTokenProvider.getRemainingExpirationSeconds(tokenPair.getAccessToken());
        assertTrue(remaining > 0 && remaining <= 3600);
    }
}
