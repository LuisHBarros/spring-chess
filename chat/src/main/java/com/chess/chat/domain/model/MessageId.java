package com.chess.chat.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class MessageId {
    private final UUID value;

    private MessageId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("MessageId value cannot be null");
        }
        this.value = value;
    }

    public static MessageId generate() {
        return new MessageId(UUID.randomUUID());
    }

    public static MessageId from(UUID value) {
        return new MessageId(value);
    }

    public static MessageId fromString(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            throw new IllegalArgumentException("MessageId string cannot be null or empty");
        }
        return new MessageId(UUID.fromString(uuidString.trim()));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageId messageId = (MessageId) o;
        return Objects.equals(value, messageId.value);
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
