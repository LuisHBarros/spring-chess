package com.chess.chat.infrastructure.web.dto;

import com.chess.chat.domain.model.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class SendMessageRequestDto {

    @NotNull(message = "ChatRoomId is required")
    private UUID chatRoomId;

    @NotNull(message = "SenderId is required")
    private UUID senderId;

    @NotBlank(message = "Message content is required")
    private String content;

    private MessageType type = MessageType.TEXT;

    private UUID replyToMessageId;

    public SendMessageRequestDto() {
    }

    public SendMessageRequestDto(UUID chatRoomId, UUID senderId, String content, MessageType type, UUID replyToMessageId) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.type = type != null ? type : MessageType.TEXT;
        this.replyToMessageId = replyToMessageId;
    }

    public UUID getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(UUID chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public UUID getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(UUID replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }
}
