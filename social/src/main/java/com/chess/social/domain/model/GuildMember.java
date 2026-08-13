package com.chess.social.domain.model;

import java.time.Instant;
import java.util.Objects;

public class GuildMember {
    private final UserId userId;
    private GuildRole role;
    private RankId assignedRankId;
    private Avatar avatar;
    private final Instant joinedAt;

    private GuildMember(UserId userId, GuildRole role, RankId assignedRankId, Avatar avatar, Instant joinedAt) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (role == null) {
            throw new IllegalArgumentException("GuildRole cannot be null");
        }
        if (joinedAt == null) {
            throw new IllegalArgumentException("JoinedAt timestamp cannot be null");
        }
        this.userId = userId;
        this.role = role;
        this.assignedRankId = assignedRankId;
        this.avatar = avatar != null ? avatar : Avatar.defaultAvatar();
        this.joinedAt = joinedAt;
    }

    public static GuildMember of(UserId userId, RankId rankId, Instant joinedAt) {
        return new GuildMember(userId, GuildRole.MEMBER, rankId, Avatar.defaultAvatar(), joinedAt);
    }

    public static GuildMember create(UserId userId, GuildRole role, RankId assignedRankId) {
        return new GuildMember(userId, role, assignedRankId, Avatar.defaultAvatar(), Instant.now());
    }

    public static GuildMember create(UserId userId, GuildRole role, RankId assignedRankId, Avatar avatar) {
        return new GuildMember(userId, role, assignedRankId, avatar, Instant.now());
    }

    public static GuildMember reconstitute(UserId userId, GuildRole role, RankId assignedRankId, Avatar avatar, Instant joinedAt) {
        return new GuildMember(userId, role, assignedRankId, avatar, joinedAt);
    }

    public GuildMember withRank(RankId newRankId) {
        return new GuildMember(this.userId, this.role, newRankId, this.avatar, this.joinedAt);
    }

    public void updateRole(GuildRole newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("GuildRole cannot be null");
        }
        this.role = newRole;
    }

    public void assignRank(RankId rankId) {
        this.assignedRankId = rankId;
    }

    public void updateAvatar(Avatar avatar) {
        if (avatar == null) {
            throw new IllegalArgumentException("Avatar cannot be null");
        }
        this.avatar = avatar;
    }

    public UserId getUserId() {
        return userId;
    }

    public GuildRole getRole() {
        return role;
    }

    public RankId getAssignedRankId() {
        return assignedRankId;
    }

    public RankId getRankId() {
        return assignedRankId;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuildMember member = (GuildMember) o;
        return Objects.equals(userId, member.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
