package com.chess.social.domain.model;

import java.util.Objects;

public final class RankName {
    private static final int MAX_LENGTH = 50;

    private final String value;

    private RankName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Rank name cannot be empty");
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Rank name cannot exceed " + MAX_LENGTH + " characters");
        }
        this.value = trimmed;
    }

    public static RankName of(String value) {
        return new RankName(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RankName rankName = (RankName) o;
        return Objects.equals(value, rankName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
