package com.chess.social.infrastructure.persistence.entity;

import com.chess.social.domain.model.GuildRank;
import com.chess.social.domain.model.RankId;
import com.chess.social.domain.model.RankName;
import com.chess.social.domain.model.RankPermission;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "guild_ranks")
public class GuildRankJpaEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "priority", nullable = false)
    private int priority;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "guild_rank_permissions", joinColumns = @JoinColumn(name = "rank_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Set<RankPermission> permissions = new HashSet<>();

    public GuildRankJpaEntity() {
    }

    public GuildRankJpaEntity(UUID id, String name, int priority, Set<RankPermission> permissions) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.permissions = permissions != null ? permissions : new HashSet<>();
    }

    public static GuildRankJpaEntity fromDomain(GuildRank rank) {
        return new GuildRankJpaEntity(
                rank.getId().getValue(),
                rank.getName().getValue(),
                rank.getPriority(),
                new HashSet<>(rank.getPermissions())
        );
    }

    public GuildRank toDomain() {
        return GuildRank.reconstitute(
                RankId.from(id),
                RankName.of(name),
                priority,
                permissions
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Set<RankPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<RankPermission> permissions) {
        this.permissions = permissions;
    }
}
