package com.chess.game.domain.model;

import java.util.Objects;

public final class GameClock {
    private final int initialTimeSeconds;
    private final int incrementSeconds;
    private final long whiteTimeRemainingMs;
    private final long blackTimeRemainingMs;

    private GameClock(int initialTimeSeconds, int incrementSeconds,
                      long whiteTimeRemainingMs, long blackTimeRemainingMs) {
        if (initialTimeSeconds < 0) {
            throw new IllegalArgumentException("Initial time cannot be negative");
        }
        if (incrementSeconds < 0) {
            throw new IllegalArgumentException("Increment cannot be negative");
        }
        this.initialTimeSeconds = initialTimeSeconds;
        this.incrementSeconds = incrementSeconds;
        this.whiteTimeRemainingMs = whiteTimeRemainingMs;
        this.blackTimeRemainingMs = blackTimeRemainingMs;
    }

    public static GameClock create(int initialTimeSeconds, int incrementSeconds) {
        long initialMs = initialTimeSeconds * 1000L;
        return new GameClock(initialTimeSeconds, incrementSeconds, initialMs, initialMs);
    }

    public static GameClock reconstitute(int initialTimeSeconds, int incrementSeconds,
                                          long whiteTimeRemainingMs, long blackTimeRemainingMs) {
        return new GameClock(initialTimeSeconds, incrementSeconds, whiteTimeRemainingMs, blackTimeRemainingMs);
    }

    public GameClock decrementTime(Color color, long elapsedMs) {
        if (color == Color.WHITE) {
            return new GameClock(initialTimeSeconds, incrementSeconds,
                    whiteTimeRemainingMs - elapsedMs, blackTimeRemainingMs);
        } else {
            return new GameClock(initialTimeSeconds, incrementSeconds,
                    whiteTimeRemainingMs, blackTimeRemainingMs - elapsedMs);
        }
    }

    public GameClock addIncrement(Color color) {
        long incrementMs = incrementSeconds * 1000L;
        if (color == Color.WHITE) {
            return new GameClock(initialTimeSeconds, incrementSeconds,
                    whiteTimeRemainingMs + incrementMs, blackTimeRemainingMs);
        } else {
            return new GameClock(initialTimeSeconds, incrementSeconds,
                    whiteTimeRemainingMs, blackTimeRemainingMs + incrementMs);
        }
    }

    public boolean isTimeUp(Color color) {
        return color == Color.WHITE ? whiteTimeRemainingMs <= 0 : blackTimeRemainingMs <= 0;
    }

    public int getInitialTimeSeconds() { return initialTimeSeconds; }
    public int getIncrementSeconds() { return incrementSeconds; }
    public long getWhiteTimeRemainingMs() { return whiteTimeRemainingMs; }
    public long getBlackTimeRemainingMs() { return blackTimeRemainingMs; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameClock gameClock = (GameClock) o;
        return initialTimeSeconds == gameClock.initialTimeSeconds
                && incrementSeconds == gameClock.incrementSeconds
                && whiteTimeRemainingMs == gameClock.whiteTimeRemainingMs
                && blackTimeRemainingMs == gameClock.blackTimeRemainingMs;
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialTimeSeconds, incrementSeconds, whiteTimeRemainingMs, blackTimeRemainingMs);
    }

    @Override
    public String toString() {
        return String.format("GameClock{initial=%ds, increment=%ds, white=%dms, black=%dms}",
                initialTimeSeconds, incrementSeconds, whiteTimeRemainingMs, blackTimeRemainingMs);
    }
}
