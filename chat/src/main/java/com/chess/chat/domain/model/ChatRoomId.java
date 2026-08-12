package com.chess.chat.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class ChatRoomId {
    private final UUID value;

    private ChatRoomId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("ChatRoomId value cannot be null");
        }
        this.value = value;
    }

    public static ChatRoomId generate() {
        return new ChatRoomId(UUID.randomUUID());
    }

    public static ChatRoomId from(UUID value) {
        return new ChatRoomId(value);
    }

    public static ChatRoomId fromString(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new IllegalArgumentException("ChatRoomId string cannot be null or empty");
        }
        return new ChatRoomId(UUID.fromString(uuidString.trim()));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoomId that = (ChatRoomId) o;
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
