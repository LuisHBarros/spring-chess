package com.chess.game.infrastructure.persistence.repository;

import com.chess.game.domain.model.GameStatus;
import com.chess.game.infrastructure.persistence.entity.GameJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataGameRepository extends JpaRepository<GameJpaEntity, UUID> {
    @Query("SELECT g FROM GameJpaEntity g WHERE g.whitePlayerId = :playerId OR g.blackPlayerId = :playerId")
    List<GameJpaEntity> findAllByPlayerId(@Param("playerId") UUID playerId);

    List<GameJpaEntity> findByStatus(GameStatus status);
}
