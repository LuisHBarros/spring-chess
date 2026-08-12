package com.chess.chat.domain.model;

import java.time.Instant;
import java.util.Objects;

public final class MessageReaction {
    private final UserId userId;
    private final String emoji;
    private final Instant createdAt;

    private MessageReaction(UserId userId, String emoji, Instant createdAt) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null in reaction");
        }
        if (emoji == null || emoji.trim().isEmpty()) {
            throw new IllegalArgumentException("Emoji cannot be null or empty in reaction");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("CreatedAt cannot be null in reaction");
        }
        this.userId = userId;
        this.emoji = emoji.trim();
        this.createdAt = createdAt;
    }

    public static MessageReaction create(UserId userId, String emoji) {
        return new MessageReaction(userId, emoji, Instant.now());
    }

    public static MessageReaction reconstitute(UserId userId, String emoji, Instant createdAt) {
        return new MessageReaction(userId, emoji, createdAt);
    }

    public UserId getUserId() {
        return userId;
    }

    public String getEmoji() {
        return emoji;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageReaction reaction = (MessageReaction) o;
        return Objects.equals(userId, reaction.userId) && Objects.equals(emoji, reaction.emoji);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, emoji);
    }
}
