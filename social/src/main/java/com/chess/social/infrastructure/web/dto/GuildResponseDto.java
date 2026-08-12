package com.chess.social.infrastructure.web.dto;

import com.chess.social.domain.model.Guild;
import com.chess.social.domain.model.GuildCategory;
import com.chess.social.domain.model.GuildMember;
import com.chess.social.domain.model.GuildRank;
import com.chess.social.domain.model.GuildRole;
import com.chess.social.domain.model.RankPermission;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class GuildResponseDto {
    private UUID id;
    private String name;
    private String description;
    private UUID ownerId;
    private String avatarUrl;
    private List<CategoryResponseDto> categories;
    private List<GuildMemberResponseDto> members;
    private Instant createdAt;
    private Instant updatedAt;

    public GuildResponseDto() {
    }

    public GuildResponseDto(
            UUID id,
            String name,
            String description,
            UUID ownerId,
            String avatarUrl,
            List<CategoryResponseDto> categories,
            List<GuildMemberResponseDto> members,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.avatarUrl = avatarUrl;
        this.categories = categories;
        this.members = members;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static GuildResponseDto fromDomain(Guild guild) {
        List<CategoryResponseDto> catDtos = guild.getCategories().stream()
                .map(CategoryResponseDto::fromDomain)
                .collect(Collectors.toList());

        List<GuildMemberResponseDto> memDtos = guild.getMembers().stream()
                .map(GuildMemberResponseDto::fromDomain)
                .collect(Collectors.toList());

        return new GuildResponseDto(
                guild.getId().getValue(),
                guild.getName().getValue(),
                guild.getDescription(),
                guild.getOwnerId().getValue(),
                guild.getAvatar() != null ? guild.getAvatar().getUrl() : null,
                catDtos,
                memDtos,
                guild.getCreatedAt(),
                guild.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public List<CategoryResponseDto> getCategories() {
        return categories;
    }

    public List<GuildMemberResponseDto> getMembers() {
        return members;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static class CategoryResponseDto {
        private UUID id;
        private String name;
        private String description;
        private List<RankResponseDto> ranks;

        public CategoryResponseDto() {
        }

        public CategoryResponseDto(UUID id, String name, String description, List<RankResponseDto> ranks) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.ranks = ranks;
        }

        public static CategoryResponseDto fromDomain(GuildCategory category) {
            List<RankResponseDto> rankDtos = category.getRanks().stream()
                    .map(RankResponseDto::fromDomain)
                    .collect(Collectors.toList());

            return new CategoryResponseDto(
                    category.getId().getValue(),
                    category.getName().getValue(),
                    category.getDescription(),
                    rankDtos
            );
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public List<RankResponseDto> getRanks() {
            return ranks;
        }
    }

    public static class RankResponseDto {
        private UUID id;
        private String name;
        private int priority;
        private Set<RankPermission> permissions;

        public RankResponseDto() {
        }

        public RankResponseDto(UUID id, String name, int priority, Set<RankPermission> permissions) {
            this.id = id;
            this.name = name;
            this.priority = priority;
            this.permissions = permissions;
        }

        public static RankResponseDto fromDomain(GuildRank rank) {
            return new RankResponseDto(
                    rank.getId().getValue(),
                    rank.getName().getValue(),
                    rank.getPriority(),
                    rank.getPermissions()
            );
        }

        public UUID getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getPriority() {
            return priority;
        }

        public Set<RankPermission> getPermissions() {
            return permissions;
        }
    }

    public static class GuildMemberResponseDto {
        private UUID userId;
        private GuildRole role;
        private UUID assignedRankId;
        private String avatarUrl;
        private Instant joinedAt;

        public GuildMemberResponseDto() {
        }

        public GuildMemberResponseDto(UUID userId, GuildRole role, UUID assignedRankId, String avatarUrl, Instant joinedAt) {
            this.userId = userId;
            this.role = role;
            this.assignedRankId = assignedRankId;
            this.avatarUrl = avatarUrl;
            this.joinedAt = joinedAt;
        }

        public static GuildMemberResponseDto fromDomain(GuildMember member) {
            return new GuildMemberResponseDto(
                    member.getUserId().getValue(),
                    member.getRole(),
                    member.getAssignedRankId() != null ? member.getAssignedRankId().getValue() : null,
                    member.getAvatar() != null ? member.getAvatar().getUrl() : null,
                    member.getJoinedAt()
            );
        }

        public UUID getUserId() {
            return userId;
        }

        public GuildRole getRole() {
            return role;
        }

        public UUID getAssignedRankId() {
            return assignedRankId;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public Instant getJoinedAt() {
            return joinedAt;
        }
    }
}
