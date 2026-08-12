package com.chess.social.infrastructure.persistence.repository;

import com.chess.social.domain.model.FriendshipStatus;
import com.chess.social.infrastructure.persistence.entity.FriendshipJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataFriendshipRepository extends JpaRepository<FriendshipJpaEntity, UUID> {

    @Query("SELECT f FROM FriendshipJpaEntity f WHERE (f.requesterId = :u1 AND f.addresseeId = :u2) OR (f.requesterId = :u2 AND f.addresseeId = :u1)")
    Optional<FriendshipJpaEntity> findBetweenUsers(@Param("u1") UUID u1, @Param("u2") UUID u2);

    @Query("SELECT f FROM FriendshipJpaEntity f WHERE (f.requesterId = :userId OR f.addresseeId = :userId) AND f.status = :status")
    List<FriendshipJpaEntity> findAllByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") FriendshipStatus status);

    @Query("SELECT f FROM FriendshipJpaEntity f WHERE f.requesterId = :userId OR f.addresseeId = :userId")
    List<FriendshipJpaEntity> findAllByUserId(@Param("userId") UUID userId);
}
