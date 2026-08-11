package com.chess.auth.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisTokenBlacklistAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisTokenBlacklistAdapter blacklistAdapter;

    @BeforeEach
    void setUp() {
        blacklistAdapter = new RedisTokenBlacklistAdapter(redisTemplate);
    }

    @Test
    @DisplayName("Should blacklist token with TTL in Redis")
    void shouldBlacklistToken() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        blacklistAdapter.blacklistToken("sample-access-token", 1800);

        verify(valueOperations).set("blacklist:token:sample-access-token", "revoked", 1800, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should check if token is blacklisted in Redis")
    void shouldCheckIfTokenIsBlacklisted() {
        when(redisTemplate.hasKey("blacklist:token:sample-access-token")).thenReturn(true);
        when(redisTemplate.hasKey("blacklist:token:active-access-token")).thenReturn(false);

        assertTrue(blacklistAdapter.isBlacklisted("sample-access-token"));
        assertFalse(blacklistAdapter.isBlacklisted("active-access-token"));
        assertFalse(blacklistAdapter.isBlacklisted(null));
        assertFalse(blacklistAdapter.isBlacklisted(""));
    }
}
