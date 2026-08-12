package com.chess.chat.domain.model;

import java.time.Instant;
import java.util.Objects;

public class ChatParticipant {
    private final UserId userId;
    private ParticipantRole role;
    private final Instant joinedAt;
    private Instant lastReadAt;
    private boolean muted;

    private ChatParticipant(UserId userId, ParticipantRole role, Instant joinedAt, Instant lastReadAt, boolean muted) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null for ChatParticipant");
        }
        if (role == null) {
            throw new IllegalArgumentException("ParticipantRole cannot be null");
        }
        if (joinedAt == null) {
            throw new IllegalArgumentException("JoinedAt cannot be null");
        }
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
        this.lastReadAt = lastReadAt != null ? lastReadAt : joinedAt;
        this.muted = muted;
    }

    public static ChatParticipant create(UserId userId, ParticipantRole role) {
        Instant now = Instant.now();
        return new ChatParticipant(userId, role, now, now, false);
    }

    public static ChatParticipant reconstitute(
            UserId userId,
            ParticipantRole role,
            Instant joinedAt,
            Instant lastReadAt,
            boolean muted) {
        return new ChatParticipant(userId, role, joinedAt, lastReadAt, muted);
    }

    public void updateRole(ParticipantRole newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("New ParticipantRole cannot be null");
        }
        this.role = newRole;
    }

    public void updateLastRead() {
        this.lastReadAt = Instant.now();
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public UserId getUserId() {
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
        ChatParticipant participant = (ChatParticipant) o;
        return Objects.equals(userId, participant.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
