package com.chess.chat.infrastructure.web.dto;

import com.chess.chat.domain.model.MessageReaction;

import java.time.Instant;
import java.util.UUID;

public class MessageReactionResponseDto {
    private UUID userId;
    private String emoji;
    private Instant createdAt;

    public MessageReactionResponseDto() {
    }

    public MessageReactionResponseDto(UUID userId, String emoji, Instant createdAt) {
        this.userId = userId;
        this.emoji = emoji;
        this.createdAt = createdAt;
    }

    public static MessageReactionResponseDto fromDomain(MessageReaction reaction) {
        return new MessageReactionResponseDto(
                reaction.getUserId().getValue(),
                reaction.getEmoji(),
                reaction.getCreatedAt()
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
}
