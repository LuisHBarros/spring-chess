package com.chess.social.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class GuildId {
    private final UUID value;

    private GuildId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("GuildId value cannot be null");
        }
        this.value = value;
    }

    public static GuildId generate() {
        return new GuildId(UUID.randomUUID());
    }

    public static GuildId from(UUID value) {
        return new GuildId(value);
    }

    public static GuildId fromString(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new IllegalArgumentException("GuildId string cannot be null or empty");
        }
        return new GuildId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuildId guildId = (GuildId) o;
        return Objects.equals(value, guildId.value);
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
