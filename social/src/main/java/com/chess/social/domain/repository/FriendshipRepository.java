package com.chess.social.domain.repository;

import com.chess.social.domain.model.Friendship;
import com.chess.social.domain.model.FriendshipId;
import com.chess.social.domain.model.FriendshipStatus;
import com.chess.social.domain.model.UserId;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository {
    Friendship save(Friendship friendship);

    Optional<Friendship> findById(FriendshipId id);

    Optional<Friendship> findBetweenUsers(UserId requesterId, UserId addresseeId);

    List<Friendship> findAllByUserIdAndStatus(UserId userId, FriendshipStatus status);

    List<Friendship> findAllByUserId(UserId userId);

    void delete(Friendship friendship);
}
