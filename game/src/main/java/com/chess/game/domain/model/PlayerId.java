package com.chess.game.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class PlayerId {
    private final UUID value;

    private PlayerId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("PlayerId value cannot be null");
        }
        this.value = value;
    }

    public static PlayerId generate() {
        return new PlayerId(UUID.randomUUID());
    }

    public static PlayerId from(UUID value) {
        return new PlayerId(value);
    }

    public static PlayerId fromString(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new IllegalArgumentException("PlayerId string cannot be null or empty");
        }
        return new PlayerId(UUID.fromString(uuidString));
    }

    public UUID getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerId playerId = (PlayerId) o;
        return Objects.equals(value, playerId.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value.toString(); }
}
