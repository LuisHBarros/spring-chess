package com.chess.game.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class GameId {
    private final UUID value;

    private GameId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("GameId value cannot be null");
        }
        this.value = value;
    }

    public static GameId generate() {
        return new GameId(UUID.randomUUID());
    }

    public static GameId from(UUID value) {
        return new GameId(value);
    }

    public static GameId fromString(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new IllegalArgumentException("GameId string cannot be null or empty");
        }
        return new GameId(UUID.fromString(uuidString));
    }

    public UUID getValue() { return value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameId gameId = (GameId) o;
        return Objects.equals(value, gameId.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value.toString(); }
}
