package com.chess.chat.infrastructure.persistence.repository;

import com.chess.chat.infrastructure.persistence.entity.ChatRoomJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataChatRoomRepository extends JpaRepository<ChatRoomJpaEntity, UUID> {

    @Query("SELECT r FROM ChatRoomJpaEntity r JOIN r.participants p1 JOIN r.participants p2 " +
           "WHERE r.type = 'DIRECT' AND p1.userId = :userA AND p2.userId = :userB")
    Optional<ChatRoomJpaEntity> findDirectRoomBetweenUsers(@Param("userA") UUID userA, @Param("userB") UUID userB);

    @Query("SELECT DISTINCT r FROM ChatRoomJpaEntity r JOIN r.participants p WHERE p.userId = :userId")
    List<ChatRoomJpaEntity> findByParticipantUserId(@Param("userId") UUID userId);

    List<ChatRoomJpaEntity> findByTargetReferenceId(String targetReferenceId);
}
