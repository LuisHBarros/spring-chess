package com.chess.social.domain.model;

import com.chess.social.domain.exception.InvalidFriendshipTransitionException;
import com.chess.social.domain.exception.SelfFriendshipException;

import java.time.Instant;
import java.util.Objects;

public class Friendship {
    private final FriendshipId id;
    private final UserId requesterId;
    private final UserId addresseeId;
    private FriendshipStatus status;
    private UserId actionUserId;
    private final Instant createdAt;
    private Instant updatedAt;

    private Friendship(
            FriendshipId id,
            UserId requesterId,
            UserId addresseeId,
            FriendshipStatus status,
            UserId actionUserId,
            Instant createdAt,
            Instant updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("FriendshipId cannot be null");
        }
        if (requesterId == null || addresseeId == null) {
            throw new IllegalArgumentException("RequesterId and AddresseeId cannot be null");
        }
        if (requesterId.equals(addresseeId)) {
            throw new SelfFriendshipException("A user cannot send a friend request to themselves");
        }
        if (status == null) {
            throw new IllegalArgumentException("FriendshipStatus cannot be null");
        }
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("Timestamps cannot be null");
        }

        this.id = id;
        this.requesterId = requesterId;
        this.addresseeId = addresseeId;
        this.status = status;
        this.actionUserId = actionUserId != null ? actionUserId : requesterId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Friendship request(UserId requesterId, UserId addresseeId) {
        Instant now = Instant.now();
        return new Friendship(
                FriendshipId.generate(),
                requesterId,
                addresseeId,
                FriendshipStatus.PENDING,
                requesterId,
                now,
                now
        );
    }

    public static Friendship reconstitute(
            FriendshipId id,
            UserId requesterId,
            UserId addresseeId,
            FriendshipStatus status,
            UserId actionUserId,
            Instant createdAt,
            Instant updatedAt) {
        return new Friendship(id, requesterId, addresseeId, status, actionUserId, createdAt, updatedAt);
    }

    public void accept(UserId actorId) {
        if (!addresseeId.equals(actorId)) {
            throw new InvalidFriendshipTransitionException("Only the recipient of a friend request can accept it");
        }
        if (status != FriendshipStatus.PENDING) {
            throw new InvalidFriendshipTransitionException("Cannot accept friend request when status is " + status);
        }
        this.status = FriendshipStatus.ACCEPTED;
        this.actionUserId = actorId;
        this.updatedAt = Instant.now();
    }

    public void decline(UserId actorId) {
        if (!addresseeId.equals(actorId)) {
            throw new InvalidFriendshipTransitionException("Only the recipient of a friend request can decline it");
        }
        if (status != FriendshipStatus.PENDING) {
            throw new InvalidFriendshipTransitionException("Cannot decline friend request when status is " + status);
        }
        this.status = FriendshipStatus.DECLINED;
        this.actionUserId = actorId;
        this.updatedAt = Instant.now();
    }

    public void block(UserId actorId) {
        if (!isParticipant(actorId)) {
            throw new InvalidFriendshipTransitionException("Only a participant of the friendship can block the relationship");
        }
        this.status = FriendshipStatus.BLOCKED;
        this.actionUserId = actorId;
        this.updatedAt = Instant.now();
    }

    public void unblock(UserId actorId) {
        if (status != FriendshipStatus.BLOCKED) {
            throw new InvalidFriendshipTransitionException("Friendship is not currently blocked");
        }
        if (!actorId.equals(actionUserId)) {
            throw new InvalidFriendshipTransitionException("Only the user who initiated the block can unblock");
        }
        this.status = FriendshipStatus.DECLINED;
        this.actionUserId = actorId;
        this.updatedAt = Instant.now();
    }

    public boolean isParticipant(UserId userId) {
        return requesterId.equals(userId) || addresseeId.equals(userId);
    }

    public boolean isBetween(UserId userA, UserId userB) {
        return (requesterId.equals(userA) && addresseeId.equals(userB)) ||
               (requesterId.equals(userB) && addresseeId.equals(userA));
    }

    public FriendshipId getId() {
        return id;
    }

    public UserId getRequesterId() {
        return requesterId;
    }

    public UserId getAddresseeId() {
        return addresseeId;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public UserId getActionUserId() {
        return actionUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
