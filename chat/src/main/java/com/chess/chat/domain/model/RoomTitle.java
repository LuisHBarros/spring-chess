package com.chess.chat.domain.model;

import java.util.Objects;

public final class RoomTitle {
    public static final int MAX_LENGTH = 100;
    private final String value;

    private RoomTitle(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Room title cannot be null or empty");
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Room title exceeds maximum length of " + MAX_LENGTH + " characters");
        }
        this.value = trimmed;
    }

    public static RoomTitle of(String value) {
        return new RoomTitle(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomTitle roomTitle = (RoomTitle) o;
        return Objects.equals(value, roomTitle.value);
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
