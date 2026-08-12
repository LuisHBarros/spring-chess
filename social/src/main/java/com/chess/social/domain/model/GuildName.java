package com.chess.social.domain.model;

import java.util.Objects;

public final class GuildName {
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 50;

    private final String value;

    private GuildName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Guild name cannot be empty");
        }
        String trimmed = value.trim();
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Guild name must be between %d and %d characters", MIN_LENGTH, MAX_LENGTH)
            );
        }
        this.value = trimmed;
    }

    public static GuildName of(String value) {
        return new GuildName(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuildName guildName = (GuildName) o;
        return Objects.equals(value, guildName.value);
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
