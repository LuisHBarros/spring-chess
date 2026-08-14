package com.chess.auth.infrastructure.security;

import com.chess.auth.domain.exception.RateLimitExceededException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

@Component
public class RateLimiterService {

    private static final String RATE_LIMIT_PREFIX = "rate_limit:login:";
    private static final int MAX_ATTEMPTS = 5;
    private static final long TIME_WINDOW_SECONDS = 60;
    private static final long TIME_WINDOW_MS = TimeUnit.SECONDS.toMillis(TIME_WINDOW_SECONDS);

    private final StringRedisTemplate redisTemplate;
    private final Map<String, long[]> inMemoryFallbackMap = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this(redisTemplate, System::currentTimeMillis);
    }

    RateLimiterService(StringRedisTemplate redisTemplate, LongSupplier clock) {
        this.redisTemplate = redisTemplate;
        this.clock = clock;
    }

    public void checkRateLimit(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        String redisKey = RATE_LIMIT_PREFIX + key;

        try {
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count != null && count == 1) {
                redisTemplate.expire(redisKey, TIME_WINDOW_SECONDS, TimeUnit.SECONDS);
            }
            if (count != null && count > MAX_ATTEMPTS) {
                throw new RateLimitExceededException("Too many login attempts. Please try again in 1 minute.");
            }
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            // In-memory fallback if Redis is unavailable or un-mocked
            long[] entry = inMemoryFallbackMap.compute(key, (k, existing) -> {
                long now = clock.getAsLong();
                if (existing == null || now - existing[1] >= TIME_WINDOW_MS) {
                    return new long[]{1, now};
                }
                existing[0]++;
                return existing;
            });
            if (entry[0] > MAX_ATTEMPTS) {
                throw new RateLimitExceededException("Too many login attempts. Please try again in 1 minute.");
            }
        }
    }
}
