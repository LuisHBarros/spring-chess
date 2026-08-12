package com.chess.social.infrastructure.persistence.entity;

import com.chess.social.domain.model.Avatar;
import com.chess.social.domain.model.GuildMember;
import com.chess.social.domain.model.GuildRole;
import com.chess.social.domain.model.RankId;
import com.chess.social.domain.model.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "guild_members")
public class GuildMemberJpaEntity {

    @Id
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private GuildRole role;

    @Column(name = "assigned_rank_id")
    private UUID assignedRankId;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    public GuildMemberJpaEntity() {
    }

    public GuildMemberJpaEntity(UUID userId, GuildRole role, UUID assignedRankId, String avatarUrl, Instant joinedAt) {
        this.userId = userId;
        this.role = role;
        this.assignedRankId = assignedRankId;
        this.avatarUrl = avatarUrl;
        this.joinedAt = joinedAt;
    }

    public static GuildMemberJpaEntity fromDomain(GuildMember member) {
        return new GuildMemberJpaEntity(
                member.getUserId().getValue(),
                member.getRole(),
                member.getAssignedRankId() != null ? member.getAssignedRankId().getValue() : null,
                member.getAvatar() != null ? member.getAvatar().getUrl() : null,
                member.getJoinedAt()
        );
    }

    public GuildMember toDomain() {
        return GuildMember.reconstitute(
                UserId.from(userId),
                role,
                assignedRankId != null ? RankId.from(assignedRankId) : null,
                avatarUrl != null ? Avatar.of(avatarUrl) : Avatar.defaultAvatar(),
                joinedAt
        );
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public GuildRole getRole() {
        return role;
    }

    public void setRole(GuildRole role) {
        this.role = role;
    }

    public UUID getAssignedRankId() {
        return assignedRankId;
    }

    public void setAssignedRankId(UUID assignedRankId) {
        this.assignedRankId = assignedRankId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }
}
