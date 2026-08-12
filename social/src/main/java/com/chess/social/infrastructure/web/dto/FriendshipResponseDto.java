package com.chess.social.infrastructure.web.dto;

import com.chess.social.domain.model.Friendship;
import com.chess.social.domain.model.FriendshipStatus;

import java.time.Instant;
import java.util.UUID;

public class FriendshipResponseDto {
    private UUID id;
    private UUID requesterId;
    private UUID addresseeId;
    private FriendshipStatus status;
    private UUID actionUserId;
    private Instant createdAt;
    private Instant updatedAt;

    public FriendshipResponseDto() {
    }

    public FriendshipResponseDto(UUID id, UUID requesterId, UUID addresseeId, FriendshipStatus status, UUID actionUserId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.addresseeId = addresseeId;
        this.status = status;
        this.actionUserId = actionUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FriendshipResponseDto fromDomain(Friendship friendship) {
        return new FriendshipResponseDto(
                friendship.getId().getValue(),
                friendship.getRequesterId().getValue(),
                friendship.getAddresseeId().getValue(),
                friendship.getStatus(),
                friendship.getActionUserId().getValue(),
                friendship.getCreatedAt(),
                friendship.getUpdatedAt()
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
