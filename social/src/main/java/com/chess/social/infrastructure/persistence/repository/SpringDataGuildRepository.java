package com.chess.social.infrastructure.persistence.repository;

import com.chess.social.infrastructure.persistence.entity.GuildJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataGuildRepository extends JpaRepository<GuildJpaEntity, UUID> {

    Optional<GuildJpaEntity> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT g FROM GuildJpaEntity g JOIN g.members m WHERE m.userId = :userId")
    List<GuildJpaEntity> findAllByMemberId(@Param("userId") UUID userId);
}
