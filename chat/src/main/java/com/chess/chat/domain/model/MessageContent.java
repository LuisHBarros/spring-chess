package com.chess.chat.domain.model;

import com.chess.chat.domain.exception.InvalidMessageContentException;

import java.util.Objects;

public final class MessageContent {
    public static final int MAX_LENGTH = 2000;
    private final String value;

    private MessageContent(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidMessageContentException("Message content cannot be null or empty");
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_LENGTH) {
            throw new InvalidMessageContentException("Message content exceeds maximum allowed length of " + MAX_LENGTH + " characters");
        }
        this.value = trimmed;
    }

    public static MessageContent of(String value) {
        return new MessageContent(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageContent that = (MessageContent) o;
        return Objects.equals(value, that.value);
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
