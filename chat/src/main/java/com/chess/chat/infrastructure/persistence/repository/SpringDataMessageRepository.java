package com.chess.chat.infrastructure.persistence.repository;

import com.chess.chat.infrastructure.persistence.entity.MessageJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataMessageRepository extends JpaRepository<MessageJpaEntity, UUID> {

    List<MessageJpaEntity> findByChatRoomIdOrderBySentAtDescSequenceDesc(UUID chatRoomId, Pageable pageable);

    @Query("SELECT m FROM MessageJpaEntity m WHERE m.chatRoomId = :chatRoomId AND m.senderId <> :userId AND m.status <> 'READ' ORDER BY m.sentAt ASC, m.sequence ASC")
    List<MessageJpaEntity> findUnreadMessages(@Param("chatRoomId") UUID chatRoomId, @Param("userId") UUID userId);

    @Query("SELECT COUNT(m) FROM MessageJpaEntity m WHERE m.chatRoomId = :chatRoomId AND m.senderId <> :userId AND m.status <> 'READ'")
    long countUnreadMessages(@Param("chatRoomId") UUID chatRoomId, @Param("userId") UUID userId);

    Optional<MessageJpaEntity> findTopByChatRoomIdOrderBySequenceDesc(UUID chatRoomId);

    void deleteAllByChatRoomId(UUID chatRoomId);
}
