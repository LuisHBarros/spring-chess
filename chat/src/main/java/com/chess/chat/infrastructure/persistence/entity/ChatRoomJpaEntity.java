package com.chess.chat.infrastructure.persistence.entity;

import com.chess.chat.domain.model.ChatParticipant;
import com.chess.chat.domain.model.ChatRoom;
import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.ChatRoomStatus;
import com.chess.chat.domain.model.ChatRoomType;
import com.chess.chat.domain.model.RoomTitle;
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
@Table(name = "chat_rooms")
public class ChatRoomJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ChatRoomType type;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    @Column(name = "target_reference_id")
    private String targetReferenceId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "chat_room_participants", joinColumns = @JoinColumn(name = "chat_room_id"))
    private List<ChatParticipantJpaEntity> participants = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChatRoomStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public ChatRoomJpaEntity() {
    }

    public ChatRoomJpaEntity(
            UUID id,
            ChatRoomType type,
            String title,
            UUID creatorId,
            String targetReferenceId,
            List<ChatParticipantJpaEntity> participants,
            ChatRoomStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.creatorId = creatorId;
        this.targetReferenceId = targetReferenceId;
        this.participants = participants != null ? participants : new ArrayList<>();
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ChatRoomJpaEntity fromDomain(ChatRoom chatRoom) {
        List<ChatParticipantJpaEntity> participantEntities = chatRoom.getParticipants().stream()
                .map(ChatParticipantJpaEntity::fromDomain)
                .collect(Collectors.toList());

        return new ChatRoomJpaEntity(
                chatRoom.getId().getValue(),
                chatRoom.getType(),
                chatRoom.getTitle() != null ? chatRoom.getTitle().getValue() : null,
                chatRoom.getCreatorId().getValue(),
                chatRoom.getTargetReferenceId(),
                participantEntities,
                chatRoom.getStatus(),
                chatRoom.getCreatedAt(),
                chatRoom.getUpdatedAt()
        );
    }

    public ChatRoom toDomain() {
        List<ChatParticipant> domainParticipants = participants.stream()
                .map(ChatParticipantJpaEntity::toDomain)
                .collect(Collectors.toList());

        RoomTitle roomTitle = (title != null && !title.isBlank()) ? RoomTitle.of(title) : null;

        return ChatRoom.reconstitute(
                ChatRoomId.from(id),
                type,
                roomTitle,
                UserId.from(creatorId),
                targetReferenceId,
                domainParticipants,
                status,
                createdAt,
                updatedAt
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

    public List<ChatParticipantJpaEntity> getParticipants() {
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
