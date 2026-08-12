package com.chess.social.domain.service;

import com.chess.social.domain.exception.FriendshipAlreadyExistsException;
import com.chess.social.domain.exception.FriendshipNotFoundException;
import com.chess.social.domain.exception.InvalidFriendshipTransitionException;
import com.chess.social.domain.model.Friendship;
import com.chess.social.domain.model.FriendshipId;
import com.chess.social.domain.model.FriendshipStatus;
import com.chess.social.domain.model.UserId;
import com.chess.social.domain.repository.FriendshipRepository;

import java.util.Optional;

public class FriendshipDomainService {
    private final FriendshipRepository friendshipRepository;

    public FriendshipDomainService(FriendshipRepository friendshipRepository) {
        if (friendshipRepository == null) {
            throw new IllegalArgumentException("FriendshipRepository cannot be null");
        }
        this.friendshipRepository = friendshipRepository;
    }

    public Friendship sendFriendRequest(UserId requesterId, UserId addresseeId) {
        Optional<Friendship> existing = friendshipRepository.findBetweenUsers(requesterId, addresseeId);

        if (existing.isPresent()) {
            Friendship friendship = existing.get();
            if (friendship.getStatus() == FriendshipStatus.BLOCKED) {
                throw new InvalidFriendshipTransitionException("Cannot send friend request: friendship is blocked");
            }
            if (friendship.getStatus() == FriendshipStatus.ACCEPTED || friendship.getStatus() == FriendshipStatus.PENDING) {
                throw new FriendshipAlreadyExistsException("A friend request or friendship already exists between these users");
            }
        }

        Friendship friendship = Friendship.request(requesterId, addresseeId);
        return friendshipRepository.save(friendship);
    }

    public Friendship acceptFriendRequest(UserId addresseeId, FriendshipId friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Friendship not found with ID: " + friendshipId));

        friendship.accept(addresseeId);
        return friendshipRepository.save(friendship);
    }

    public Friendship declineFriendRequest(UserId addresseeId, FriendshipId friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Friendship not found with ID: " + friendshipId));

        friendship.decline(addresseeId);
        return friendshipRepository.save(friendship);
    }

    public Friendship blockUser(UserId actorId, UserId targetUserId) {
        Optional<Friendship> existing = friendshipRepository.findBetweenUsers(actorId, targetUserId);

        Friendship friendship;
        if (existing.isPresent()) {
            friendship = existing.get();
            friendship.block(actorId);
        } else {
            friendship = Friendship.request(actorId, targetUserId);
            friendship.block(actorId);
        }

        return friendshipRepository.save(friendship);
    }

    public Friendship unblockUser(UserId actorId, UserId targetUserId) {
        Friendship friendship = friendshipRepository.findBetweenUsers(actorId, targetUserId)
                .orElseThrow(() -> new FriendshipNotFoundException("No friendship record found between these users"));

        friendship.unblock(actorId);
        return friendshipRepository.save(friendship);
    }

    public void removeFriendship(UserId actorId, FriendshipId friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new FriendshipNotFoundException("Friendship not found with ID: " + friendshipId));

        if (!friendship.isParticipant(actorId)) {
            throw new InvalidFriendshipTransitionException("Only participants can remove a friendship");
        }

        friendshipRepository.delete(friendship);
    }
}
