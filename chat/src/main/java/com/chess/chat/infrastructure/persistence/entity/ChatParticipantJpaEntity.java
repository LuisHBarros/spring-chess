package com.chess.chat.infrastructure.persistence.entity;

import com.chess.chat.domain.model.ChatParticipant;
import com.chess.chat.domain.model.ParticipantRole;
import com.chess.chat.domain.model.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ChatParticipantJpaEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ParticipantRole role;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "last_read_at", nullable = false)
    private Instant lastReadAt;

    @Column(name = "muted", nullable = false)
    private boolean muted;

    public ChatParticipantJpaEntity() {
    }

    public ChatParticipantJpaEntity(UUID userId, ParticipantRole role, Instant joinedAt, Instant lastReadAt, boolean muted) {
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
        this.lastReadAt = lastReadAt;
        this.muted = muted;
    }

    public static ChatParticipantJpaEntity fromDomain(ChatParticipant participant) {
        return new ChatParticipantJpaEntity(
                participant.getUserId().getValue(),
                participant.getRole(),
                participant.getJoinedAt(),
                participant.getLastReadAt(),
                participant.isMuted()
        );
    }

    public ChatParticipant toDomain() {
        return ChatParticipant.reconstitute(
                UserId.from(userId),
                role,
                joinedAt,
                lastReadAt,
                muted
        );
    }

    public UUID getUserId() {
        return userId;
    }

    public ParticipantRole getRole() {
        return role;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getLastReadAt() {
        return lastReadAt;
    }

    public boolean isMuted() {
        return muted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatParticipantJpaEntity that = (ChatParticipantJpaEntity) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
