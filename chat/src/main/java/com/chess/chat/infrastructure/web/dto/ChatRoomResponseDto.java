package com.chess.chat.infrastructure.web.dto;

import com.chess.chat.domain.model.ChatRoom;
import com.chess.chat.domain.model.ChatRoomStatus;
import com.chess.chat.domain.model.ChatRoomType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatRoomResponseDto {
    private UUID id;
    private ChatRoomType type;
    private String title;
    private UUID creatorId;
    private String targetReferenceId;
    private List<ChatParticipantResponseDto> participants;
    private ChatRoomStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public ChatRoomResponseDto() {
    }

    public ChatRoomResponseDto(
            UUID id,
            ChatRoomType type,
            String title,
            UUID creatorId,
            String targetReferenceId,
            List<ChatParticipantResponseDto> participants,
            ChatRoomStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.creatorId = creatorId;
        this.targetReferenceId = targetReferenceId;
        this.participants = participants;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ChatRoomResponseDto fromDomain(ChatRoom chatRoom) {
        List<ChatParticipantResponseDto> participantDtos = chatRoom.getParticipants().stream()
                .map(ChatParticipantResponseDto::fromDomain)
                .collect(Collectors.toList());

        return new ChatRoomResponseDto(
                chatRoom.getId().getValue(),
                chatRoom.getType(),
                chatRoom.getTitle() != null ? chatRoom.getTitle().getValue() : null,
                chatRoom.getCreatorId().getValue(),
                chatRoom.getTargetReferenceId(),
                participantDtos,
                chatRoom.getStatus(),
                chatRoom.getCreatedAt(),
                chatRoom.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public ChatRoomType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public String getTargetReferenceId() {
        return targetReferenceId;
    }

    public List<ChatParticipantResponseDto> getParticipants() {
        return participants;
    }

    public ChatRoomStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
