package com.chess.social.infrastructure.persistence.repository;

import com.chess.social.infrastructure.persistence.entity.GuildJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataGuildRepository extends JpaRepository<GuildJpaEntity, UUID> {

    @Query("SELECT DISTINCT g FROM GuildJpaEntity g " +
            "LEFT JOIN FETCH g.members " +
            "LEFT JOIN FETCH g.categories c " +
            "LEFT JOIN FETCH c.ranks r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE g.id = :id")
    Optional<GuildJpaEntity> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT DISTINCT g FROM GuildJpaEntity g " +
            "LEFT JOIN FETCH g.members " +
            "LEFT JOIN FETCH g.categories c " +
            "LEFT JOIN FETCH c.ranks r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE g.name = :name")
    Optional<GuildJpaEntity> findByName(@Param("name") String name);

    boolean existsByName(String name);

    @Query(value = "SELECT DISTINCT g FROM GuildJpaEntity g " +
            "JOIN FETCH g.members m " +
            "LEFT JOIN FETCH g.categories c " +
            "LEFT JOIN FETCH c.ranks r " +
            "LEFT JOIN FETCH r.permissions " +
            "WHERE m.userId = :userId",
            countQuery = "SELECT COUNT(DISTINCT g) FROM GuildJpaEntity g JOIN g.members m WHERE m.userId = :userId")
    Page<GuildJpaEntity> findAllByMemberId(@Param("userId") UUID userId, Pageable pageable);
}
