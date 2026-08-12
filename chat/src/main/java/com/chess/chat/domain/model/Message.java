package com.chess.chat.domain.model;

import com.chess.chat.domain.exception.UnauthorizedChatOperationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Message {
    private final MessageId id;
    private final ChatRoomId chatRoomId;
    private final UserId senderId;
    private MessageContent content;
    private final MessageType type;
    private MessageStatus status;
    private final MessageId replyToMessageId;
    private final List<MessageReaction> reactions;
    private final Instant sentAt;
    private Instant editedAt;
    private boolean deleted;

    private Message(
            MessageId id,
            ChatRoomId chatRoomId,
            UserId senderId,
            MessageContent content,
            MessageType type,
            MessageStatus status,
            MessageId replyToMessageId,
            List<MessageReaction> reactions,
            Instant sentAt,
            Instant editedAt,
            boolean deleted) {
        if (id == null) {
            throw new IllegalArgumentException("MessageId cannot be null");
        }
        if (chatRoomId == null) {
            throw new IllegalArgumentException("ChatRoomId cannot be null");
        }
        if (senderId == null) {
            throw new IllegalArgumentException("Sender UserId cannot be null");
        }
        if (content == null) {
            throw new IllegalArgumentException("MessageContent cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("MessageType cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("MessageStatus cannot be null");
        }
        if (sentAt == null) {
            throw new IllegalArgumentException("SentAt timestamp cannot be null");
        }

        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.status = status;
        this.replyToMessageId = replyToMessageId;
        this.reactions = reactions != null ? new ArrayList<>(reactions) : new ArrayList<>();
        this.sentAt = sentAt;
        this.editedAt = editedAt;
        this.deleted = deleted;
    }

    public static Message send(ChatRoomId chatRoomId, UserId senderId, MessageContent content, MessageType type, MessageId replyToMessageId) {
        Instant now = Instant.now();
        MessageId messageId = MessageId.generate();
        return new Message(
                messageId,
                chatRoomId,
                senderId,
                content,
                type != null ? type : MessageType.TEXT,
                MessageStatus.SENT,
                replyToMessageId,
                new ArrayList<>(),
                now,
                null,
                false
        );
    }

    public static Message send(ChatRoomId chatRoomId, UserId senderId, MessageContent content) {
        return send(chatRoomId, senderId, content, MessageType.TEXT, null);
    }

    public static Message reconstitute(
            MessageId id,
            ChatRoomId chatRoomId,
            UserId senderId,
            MessageContent content,
            MessageType type,
            MessageStatus status,
            MessageId replyToMessageId,
            List<MessageReaction> reactions,
            Instant sentAt,
            Instant editedAt,
            boolean deleted) {
        return new Message(id, chatRoomId, senderId, content, type, status, replyToMessageId, reactions, sentAt, editedAt, deleted);
    }

    public void edit(UserId actorId, MessageContent newContent) {
        if (deleted) {
            throw new IllegalStateException("Cannot edit a deleted message");
        }
        if (!senderId.equals(actorId)) {
            throw new UnauthorizedChatOperationException("Only the original sender can edit this message");
        }
        if (newContent == null) {
            throw new IllegalArgumentException("New MessageContent cannot be null");
        }

        this.content = newContent;
        this.status = MessageStatus.EDITED;
        this.editedAt = Instant.now();
    }

    public void delete(UserId actorId) {
        if (deleted) {
            return;
        }
        if (!senderId.equals(actorId)) {
            throw new UnauthorizedChatOperationException("Only the message sender or room admin can delete this message");
        }

        this.deleted = true;
        this.status = MessageStatus.DELETED;
        this.content = MessageContent.of("[Message Deleted]");
        this.editedAt = Instant.now();
    }

    public void addReaction(UserId actorId, String emoji) {
        if (deleted) {
            throw new IllegalStateException("Cannot react to a deleted message");
        }
        MessageReaction reaction = MessageReaction.create(actorId, emoji);
        if (!reactions.contains(reaction)) {
            reactions.add(reaction);
        }
    }

    public void removeReaction(UserId actorId, String emoji) {
        reactions.removeIf(r -> r.getUserId().equals(actorId) && r.getEmoji().equalsIgnoreCase(emoji.trim()));
    }

    public void markAsDelivered() {
        if (!deleted && status == MessageStatus.SENT) {
            this.status = MessageStatus.DELIVERED;
        }
    }

    public void markAsRead() {
        if (!deleted && (status == MessageStatus.SENT || status == MessageStatus.DELIVERED)) {
            this.status = MessageStatus.READ;
        }
    }

    public MessageId getId() {
        return id;
    }

    public ChatRoomId getChatRoomId() {
        return chatRoomId;
    }

    public UserId getSenderId() {
        return senderId;
    }

    public MessageContent getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public MessageId getReplyToMessageId() {
        return replyToMessageId;
    }

    public List<MessageReaction> getReactions() {
        return Collections.unmodifiableList(reactions);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
