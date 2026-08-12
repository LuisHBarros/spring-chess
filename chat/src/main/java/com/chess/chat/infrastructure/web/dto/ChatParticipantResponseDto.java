package com.chess.chat.infrastructure.web.dto;

import com.chess.chat.domain.model.ChatParticipant;
import com.chess.chat.domain.model.ParticipantRole;

import java.time.Instant;
import java.util.UUID;

public class ChatParticipantResponseDto {
    private UUID userId;
    private ParticipantRole role;
    private Instant joinedAt;
    private Instant lastReadAt;
    private boolean muted;

    public ChatParticipantResponseDto() {
    }

    public ChatParticipantResponseDto(UUID userId, ParticipantRole role, Instant joinedAt, Instant lastReadAt, boolean muted) {
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
        this.lastReadAt = lastReadAt;
        this.muted = muted;
    }

    public static ChatParticipantResponseDto fromDomain(ChatParticipant participant) {
        return new ChatParticipantResponseDto(
                participant.getUserId().getValue(),
                participant.getRole(),
                participant.getJoinedAt(),
                participant.getLastReadAt(),
                participant.isMuted()
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
}
