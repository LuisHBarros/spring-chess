package com.chess.social.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class FriendshipId {
    private final UUID value;

    private FriendshipId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("FriendshipId value cannot be null");
        }
        this.value = value;
    }

    public static FriendshipId generate() {
        return new FriendshipId(UUID.randomUUID());
    }

    public static FriendshipId from(UUID value) {
        return new FriendshipId(value);
    }

    public static FriendshipId fromString(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new IllegalArgumentException("FriendshipId string cannot be null or empty");
        }
        return new FriendshipId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendshipId that = (FriendshipId) o;
        return Objects.equals(value, that.value);
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
