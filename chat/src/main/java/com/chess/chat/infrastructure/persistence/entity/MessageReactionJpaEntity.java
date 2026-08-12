package com.chess.chat.infrastructure.persistence.entity;

import com.chess.chat.domain.model.MessageReaction;
import com.chess.chat.domain.model.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class MessageReactionJpaEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "emoji", nullable = false, length = 20)
    private String emoji;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public MessageReactionJpaEntity() {
    }

    public MessageReactionJpaEntity(UUID userId, String emoji, Instant createdAt) {
        this.userId = userId;
        this.emoji = emoji;
        this.createdAt = createdAt;
    }

    public static MessageReactionJpaEntity fromDomain(MessageReaction reaction) {
        return new MessageReactionJpaEntity(
                reaction.getUserId().getValue(),
                reaction.getEmoji(),
                reaction.getCreatedAt()
        );
    }

    public MessageReaction toDomain() {
        return MessageReaction.reconstitute(
                UserId.from(userId),
                emoji,
                createdAt
        );
    }

    public UUID getUserId() {
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
        MessageReactionJpaEntity that = (MessageReactionJpaEntity) o;
        return Objects.equals(userId, that.userId) && Objects.equals(emoji, that.emoji);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, emoji);
    }
}
