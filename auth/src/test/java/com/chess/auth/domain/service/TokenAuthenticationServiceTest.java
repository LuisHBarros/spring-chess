package com.chess.auth.domain.service;

import com.chess.auth.domain.exception.InvalidTokenException;
import com.chess.auth.domain.exception.UserNotFoundException;
import com.chess.auth.domain.model.*;
import com.chess.auth.domain.port.TokenBlacklistService;
import com.chess.auth.domain.port.TokenProvider;
import com.chess.auth.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationServiceTest {

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private TokenBlacklistService blacklistService;

    @Mock
    private UserRepository userRepository;

    private TokenAuthenticationService tokenAuthService;
    private User testUser;
    private AuthToken sampleAuthToken;

    @BeforeEach
    void setUp() {
        tokenAuthService = new TokenAuthenticationService(tokenProvider, blacklistService, userRepository);
        testUser = User.create(new Username("jwt_user"), new Email("jwt@chess.com"), Password.fromHash("pass"));
        sampleAuthToken = new AuthToken("access-token-123", "refresh-token-456", "Bearer", 3600);
    }

    @Test
    @DisplayName("Should generate tokens for user")
    void shouldGenerateTokens() {
        when(tokenProvider.generateTokens(testUser)).thenReturn(sampleAuthToken);

        AuthToken token = tokenAuthService.generateTokens(testUser);

        assertNotNull(token);
        assertEquals("access-token-123", token.getAccessToken());
        assertEquals("refresh-token-456", token.getRefreshToken());
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void shouldRefreshToken() {
        String refreshTokenStr = "valid-refresh-token";
        Email email = new Email("jwt@chess.com");

        when(tokenProvider.validateRefreshToken(refreshTokenStr)).thenReturn(true);
        when(tokenProvider.extractEmailFromRefreshToken(refreshTokenStr)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateTokens(testUser)).thenReturn(sampleAuthToken);

        AuthToken result = tokenAuthService.refreshToken(refreshTokenStr);

        assertNotNull(result);
        assertEquals("access-token-123", result.getAccessToken());
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when refresh token is invalid")
    void shouldThrowExceptionWhenRefreshTokenInvalid() {
        String invalidRefresh = "invalid-refresh-token";
        when(tokenProvider.validateRefreshToken(invalidRefresh)).thenReturn(false);

        assertThrows(InvalidTokenException.class, () -> tokenAuthService.refreshToken(invalidRefresh));
    }

    @Test
    @DisplayName("Should logout and blacklist valid access token")
    void shouldLogoutAndBlacklistToken() {
        String accessToken = "Bearer valid-access-token";
        String cleanToken = "valid-access-token";

        when(tokenProvider.validateAccessToken(cleanToken)).thenReturn(true);
        when(tokenProvider.getRemainingExpirationSeconds(cleanToken)).thenReturn(1800L);

        tokenAuthService.logout(accessToken);

        verify(blacklistService).blacklistToken(cleanToken, 1800L);
    }

    @Test
    @DisplayName("Should check if token is revoked")
    void shouldCheckIfTokenIsRevoked() {
        when(blacklistService.isBlacklisted("revoked-token")).thenReturn(true);

        assertTrue(tokenAuthService.isTokenRevoked("Bearer revoked-token"));
        assertTrue(tokenAuthService.isTokenRevoked(null));
    }
}
