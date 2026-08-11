package com.chess.auth.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthHealthIndicatorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private Connection connection;

    @Mock
    private RedisConnection redisConnection;

    @InjectMocks
    private AuthHealthIndicator healthIndicator;

    @Test
    @DisplayName("Should return UP status when both DB and Redis are healthy")
    void shouldReturnUpWhenBothHealthy() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(true);
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("UP", health.getDetails().get("database"));
        assertEquals("UP", health.getDetails().get("redis"));
    }

    @Test
    @DisplayName("Should return DOWN status when DB check fails")
    void shouldReturnDownWhenDbFails() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("DB Connection Error"));
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("DOWN", health.getDetails().get("database"));
        assertEquals("UP", health.getDetails().get("redis"));
    }

    @Test
    @DisplayName("Should return DOWN status when Redis check fails")
    void shouldReturnDownWhenRedisFails() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(true);
        when(redisConnectionFactory.getConnection()).thenThrow(new RuntimeException("Redis Connection Error"));

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("UP", health.getDetails().get("database"));
        assertEquals("DOWN", health.getDetails().get("redis"));
    }

    @Test
    @DisplayName("Should return DOWN status when both DB and Redis fail")
    void shouldReturnDownWhenBothFail() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("DB Error"));
        when(redisConnectionFactory.getConnection()).thenThrow(new RuntimeException("Redis Error"));

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("DOWN", health.getDetails().get("database"));
        assertEquals("DOWN", health.getDetails().get("redis"));
    }
}
