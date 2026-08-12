package com.chess.chat.infrastructure.web.dto;

import com.chess.chat.domain.model.Message;
import com.chess.chat.domain.model.MessageStatus;
import com.chess.chat.domain.model.MessageType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageResponseDto {
    private UUID id;
    private UUID chatRoomId;
    private UUID senderId;
    private String content;
    private MessageType type;
    private MessageStatus status;
    private UUID replyToMessageId;
    private List<MessageReactionResponseDto> reactions;
    private Instant sentAt;
    private Instant editedAt;
    private boolean deleted;

    public MessageResponseDto() {
    }

    public MessageResponseDto(
            UUID id,
            UUID chatRoomId,
            UUID senderId,
            String content,
            MessageType type,
            MessageStatus status,
            UUID replyToMessageId,
            List<MessageReactionResponseDto> reactions,
            Instant sentAt,
            Instant editedAt,
            boolean deleted) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.status = status;
        this.replyToMessageId = replyToMessageId;
        this.reactions = reactions;
        this.sentAt = sentAt;
        this.editedAt = editedAt;
        this.deleted = deleted;
    }

    public static MessageResponseDto fromDomain(Message message) {
        List<MessageReactionResponseDto> reactionDtos = message.getReactions().stream()
                .map(MessageReactionResponseDto::fromDomain)
                .collect(Collectors.toList());

        return new MessageResponseDto(
                message.getId().getValue(),
                message.getChatRoomId().getValue(),
                message.getSenderId().getValue(),
                message.getContent().getValue(),
                message.getType(),
                message.getStatus(),
                message.getReplyToMessageId() != null ? message.getReplyToMessageId().getValue() : null,
                reactionDtos,
                message.getSentAt(),
                message.getEditedAt(),
                message.isDeleted()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getChatRoomId() {
        return chatRoomId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public UUID getReplyToMessageId() {
        return replyToMessageId;
    }

    public List<MessageReactionResponseDto> getReactions() {
        return reactions;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getEditedAt() {
        return editedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
