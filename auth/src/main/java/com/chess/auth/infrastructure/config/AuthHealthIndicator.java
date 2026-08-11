package com.chess.auth.infrastructure.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component("authService")
public class AuthHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    public AuthHealthIndicator(DataSource dataSource, RedisConnectionFactory redisConnectionFactory) {
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();

        boolean dbUp = checkDatabase();
        boolean redisUp = checkRedis();

        builder.withDetail("database", dbUp ? "UP" : "DOWN");
        builder.withDetail("redis", redisUp ? "UP" : "DOWN");

        if (dbUp && redisUp) {
            return builder.up().build();
        }
        return builder.down().build();
    }

    private boolean checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean checkRedis() {
        try {
            redisConnectionFactory.getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
