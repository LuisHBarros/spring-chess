package com.chess.chat.infrastructure.persistence.entity;

import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.Message;
import com.chess.chat.domain.model.MessageContent;
import com.chess.chat.domain.model.MessageId;
import com.chess.chat.domain.model.MessageReaction;
import com.chess.chat.domain.model.MessageStatus;
import com.chess.chat.domain.model.MessageType;
import com.chess.chat.domain.model.UserId;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "chat_messages")
public class MessageJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "chat_room_id", nullable = false)
    private UUID chatRoomId;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MessageType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status;

    @Column(name = "reply_to_message_id")
    private UUID replyToMessageId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "message_reactions", joinColumns = @JoinColumn(name = "message_id"))
    private List<MessageReactionJpaEntity> reactions = new ArrayList<>();

    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;

    @Column(name = "edited_at")
    private Instant editedAt;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    public MessageJpaEntity() {
    }

    public MessageJpaEntity(
            UUID id,
            UUID chatRoomId,
            UUID senderId,
            String content,
            MessageType type,
            MessageStatus status,
            UUID replyToMessageId,
            List<MessageReactionJpaEntity> reactions,
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
        this.reactions = reactions != null ? reactions : new ArrayList<>();
        this.sentAt = sentAt;
        this.editedAt = editedAt;
        this.deleted = deleted;
    }

    public static MessageJpaEntity fromDomain(Message message) {
        List<MessageReactionJpaEntity> reactionEntities = message.getReactions().stream()
                .map(MessageReactionJpaEntity::fromDomain)
                .collect(Collectors.toList());

        return new MessageJpaEntity(
                message.getId().getValue(),
                message.getChatRoomId().getValue(),
                message.getSenderId().getValue(),
                message.getContent().getValue(),
                message.getType(),
                message.getStatus(),
                message.getReplyToMessageId() != null ? message.getReplyToMessageId().getValue() : null,
                reactionEntities,
                message.getSentAt(),
                message.getEditedAt(),
                message.isDeleted()
        );
    }

    public Message toDomain() {
        List<MessageReaction> domainReactions = reactions.stream()
                .map(MessageReactionJpaEntity::toDomain)
                .collect(Collectors.toList());

        return Message.reconstitute(
                MessageId.from(id),
                ChatRoomId.from(chatRoomId),
                UserId.from(senderId),
                MessageContent.of(content),
                type,
                status,
                replyToMessageId != null ? MessageId.from(replyToMessageId) : null,
                domainReactions,
                sentAt,
                editedAt,
                deleted
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

    public List<MessageReactionJpaEntity> getReactions() {
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
