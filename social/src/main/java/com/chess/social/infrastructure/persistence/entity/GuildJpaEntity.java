package com.chess.social.infrastructure.persistence.entity;

import com.chess.social.domain.model.Avatar;
import com.chess.social.domain.model.Guild;
import com.chess.social.domain.model.GuildCategory;
import com.chess.social.domain.model.GuildId;
import com.chess.social.domain.model.GuildMember;
import com.chess.social.domain.model.GuildName;
import com.chess.social.domain.model.UserId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "guilds")
public class GuildJpaEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "guild_id")
    private List<GuildCategoryJpaEntity> categories = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "guild_id")
    private List<GuildMemberJpaEntity> members = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public GuildJpaEntity() {
    }

    public GuildJpaEntity(
            UUID id,
            String name,
            String description,
            UUID ownerId,
            String avatarUrl,
            List<GuildCategoryJpaEntity> categories,
            List<GuildMemberJpaEntity> members,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.avatarUrl = avatarUrl;
        this.categories = categories != null ? categories : new ArrayList<>();
        this.members = members != null ? members : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static GuildJpaEntity fromDomain(Guild guild) {
        List<GuildCategoryJpaEntity> categoryEntities = guild.getCategories().stream()
                .map(GuildCategoryJpaEntity::fromDomain)
                .collect(Collectors.toList());

        List<GuildMemberJpaEntity> memberEntities = guild.getMembers().stream()
                .map(GuildMemberJpaEntity::fromDomain)
                .collect(Collectors.toList());

        return new GuildJpaEntity(
                guild.getId().getValue(),
                guild.getName().getValue(),
                guild.getDescription(),
                guild.getOwnerId().getValue(),
                guild.getAvatar() != null ? guild.getAvatar().getUrl() : null,
                categoryEntities,
                memberEntities,
                guild.getCreatedAt(),
                guild.getUpdatedAt()
        );
    }

    public Guild toDomain() {
        List<GuildCategory> domainCategories = categories.stream()
                .map(GuildCategoryJpaEntity::toDomain)
                .collect(Collectors.toList());

        List<GuildMember> domainMembers = members.stream()
                .map(GuildMemberJpaEntity::toDomain)
                .collect(Collectors.toList());

        return Guild.reconstitute(
                GuildId.from(id),
                GuildName.of(name),
                description,
                UserId.from(ownerId),
                avatarUrl != null ? Avatar.of(avatarUrl) : Avatar.defaultAvatar(),
                domainCategories,
                domainMembers,
                createdAt,
                updatedAt
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

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public List<GuildCategoryJpaEntity> getCategories() {
        return categories;
    }

    public void setCategories(List<GuildCategoryJpaEntity> categories) {
        this.categories = categories;
    }

    public List<GuildMemberJpaEntity> getMembers() {
        return members;
    }

    public void setMembers(List<GuildMemberJpaEntity> members) {
        this.members = members;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
