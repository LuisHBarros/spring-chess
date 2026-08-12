package com.chess.social.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class RankId {
    private final UUID value;

    private RankId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("RankId value cannot be null");
        }
        this.value = value;
    }

    public static RankId generate() {
        return new RankId(UUID.randomUUID());
    }

    public static RankId from(UUID value) {
        return new RankId(value);
    }

    public static RankId fromString(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new IllegalArgumentException("RankId string cannot be null or empty");
        }
        return new RankId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RankId rankId = (RankId) o;
        return Objects.equals(value, rankId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
