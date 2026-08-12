package com.chess.social.infrastructure.persistence.entity;

import com.chess.social.domain.model.Friendship;
import com.chess.social.domain.model.FriendshipId;
import com.chess.social.domain.model.FriendshipStatus;
import com.chess.social.domain.model.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "friendships")
public class FriendshipJpaEntity {

    @Id
    private UUID id;

    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;

    @Column(name = "addressee_id", nullable = false)
    private UUID addresseeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FriendshipStatus status;

    @Column(name = "action_user_id", nullable = false)
    private UUID actionUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public FriendshipJpaEntity() {
    }

    public FriendshipJpaEntity(
            UUID id,
            UUID requesterId,
            UUID addresseeId,
            FriendshipStatus status,
            UUID actionUserId,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.addresseeId = addresseeId;
        this.status = status;
        this.actionUserId = actionUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FriendshipJpaEntity fromDomain(Friendship friendship) {
        return new FriendshipJpaEntity(
                friendship.getId().getValue(),
                friendship.getRequesterId().getValue(),
                friendship.getAddresseeId().getValue(),
                friendship.getStatus(),
                friendship.getActionUserId().getValue(),
                friendship.getCreatedAt(),
                friendship.getUpdatedAt()
        );
    }

    public Friendship toDomain() {
        return Friendship.reconstitute(
                FriendshipId.from(id),
                UserId.from(requesterId),
                UserId.from(addresseeId),
                status,
                UserId.from(actionUserId),
                createdAt,
                updatedAt
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(UUID requesterId) {
        this.requesterId = requesterId;
    }

    public UUID getAddresseeId() {
        return addresseeId;
    }

    public void setAddresseeId(UUID addresseeId) {
        this.addresseeId = addresseeId;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    public UUID getActionUserId() {
        return actionUserId;
    }

    public void setActionUserId(UUID actionUserId) {
        this.actionUserId = actionUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
