package com.chess.auth.infrastructure.security;

import com.chess.auth.domain.exception.InvalidCredentialsException;
import com.chess.auth.domain.exception.InvalidTokenException;
import com.chess.auth.domain.exception.RateLimitExceededException;
import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.Password;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.model.Username;
import com.chess.auth.domain.port.PasswordEncoder;
import com.chess.auth.domain.repository.UserRepository;
import com.chess.auth.domain.service.TokenAuthenticationService;
import com.chess.auth.domain.service.UserLoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecuritySuiteTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private UserLoginService loginService;
    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        loginService = new UserLoginService(userRepository, passwordEncoder);
        rateLimiterService = new RateLimiterService(redisTemplate);
    }

    @Test
    @DisplayName("Security: User Enumeration Protection - Non-existent user and wrong password throw identical exception")
    void preventUserEnumerationOnLogin() {
        Email missingEmail = new Email("missing@chess.com");
        Email existingEmail = new Email("existing@chess.com");
        Password rawPassword = Password.fromRaw("password123");

        User existingUser = User.create(new Username("existing"), existingEmail, Password.fromHash("hash123"));

        when(userRepository.findByEmail(missingEmail)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(existingEmail)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(rawPassword, existingUser.getPassword())).thenReturn(false);

        InvalidCredentialsException ex1 = assertThrows(InvalidCredentialsException.class, () ->
                loginService.loginWithEmail(missingEmail, rawPassword)
        );

        InvalidCredentialsException ex2 = assertThrows(InvalidCredentialsException.class, () ->
                loginService.loginWithEmail(existingEmail, rawPassword)
        );

        assertEquals(ex1.getMessage(), ex2.getMessage());
        assertEquals("Invalid credentials", ex1.getMessage());
    }

    @Test
    @DisplayName("Security: Rate Limiter - Throws RateLimitExceededException after 5 failed attempts")
    void enforceRateLimiterThreshold() {
        String clientIp = "192.168.1.100";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L, 2L, 3L, 4L, 5L, 6L);

        // 5 allowed attempts
        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> rateLimiterService.checkRateLimit(clientIp));
        }

        // 6th attempt blocked
        assertThrows(RateLimitExceededException.class, () -> rateLimiterService.checkRateLimit(clientIp));
    }

    @Test
    @DisplayName("Security: Token Replay / Blacklist - Blacklisted token is recognized as revoked")
    void rejectBlacklistedToken() {
        RedisTokenBlacklistAdapter blacklistAdapter = new RedisTokenBlacklistAdapter(redisTemplate);
        when(redisTemplate.hasKey("blacklist:token:replay-token-123")).thenReturn(true);

        assertTrue(blacklistAdapter.isBlacklisted("replay-token-123"));
    }

    @Test
    @DisplayName("Security: Refresh Token Expiry - Rejects invalid/expired refresh token")
    void rejectExpiredRefreshToken() {
        JwtTokenProviderAdapter jwtProvider = new JwtTokenProviderAdapter(new RsaKeyPairProvider(), 3600000, 604800000);
        RedisTokenBlacklistAdapter blacklistAdapter = new RedisTokenBlacklistAdapter(redisTemplate);
        TokenAuthenticationService tokenService = new TokenAuthenticationService(jwtProvider, blacklistAdapter, userRepository);

        assertThrows(InvalidTokenException.class, () -> tokenService.refreshToken("invalid-or-expired-token"));
    }
}
