package com.chess.social.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class CategoryId {
    private final UUID value;

    private CategoryId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("CategoryId value cannot be null");
        }
        this.value = value;
    }

    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID());
    }

    public static CategoryId from(UUID value) {
        return new CategoryId(value);
    }

    public static CategoryId fromString(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new IllegalArgumentException("CategoryId string cannot be null or empty");
        }
        return new CategoryId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryId that = (CategoryId) o;
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
