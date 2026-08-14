package com.chess.social.infrastructure.persistence.entity;

import com.chess.social.domain.model.CategoryId;
import com.chess.social.domain.model.CategoryName;
import com.chess.social.domain.model.GuildCategory;
import com.chess.social.domain.model.GuildRank;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "guild_categories")
public class GuildCategoryJpaEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Set<GuildRankJpaEntity> ranks = new LinkedHashSet<>();

    public GuildCategoryJpaEntity() {
    }

    public GuildCategoryJpaEntity(UUID id, String name, String description, List<GuildRankJpaEntity> ranks) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ranks = ranks != null ? new LinkedHashSet<>(ranks) : new LinkedHashSet<>();
    }

    public static GuildCategoryJpaEntity fromDomain(GuildCategory category) {
        List<GuildRankJpaEntity> rankEntities = category.getRanks().stream()
                .map(GuildRankJpaEntity::fromDomain)
                .collect(Collectors.toList());

        return new GuildCategoryJpaEntity(
                category.getId().getValue(),
                category.getName().getValue(),
                category.getDescription(),
                rankEntities
        );
    }

    public GuildCategory toDomain() {
        List<GuildRank> domainRanks = ranks.stream()
                .map(GuildRankJpaEntity::toDomain)
                .collect(Collectors.toList());

        return GuildCategory.reconstitute(
                CategoryId.from(id),
                CategoryName.of(name),
                description,
                domainRanks
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<GuildRankJpaEntity> getRanks() {
        return ranks;
    }

    public void setRanks(Set<GuildRankJpaEntity> ranks) {
        this.ranks = ranks != null ? new LinkedHashSet<>(ranks) : new LinkedHashSet<>();
    }
}
