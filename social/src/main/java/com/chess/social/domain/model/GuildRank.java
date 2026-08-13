package com.chess.social.domain.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class GuildRank {
    private final RankId id;
    private RankName name;
    private int priority;
    private final Set<RankPermission> permissions;

    private GuildRank(RankId id, RankName name, int priority, Set<RankPermission> permissions) {
        if (id == null) {
            throw new IllegalArgumentException("RankId cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("RankName cannot be null");
        }
        if (priority < 1) {
            throw new IllegalArgumentException("Rank priority must be positive");
        }
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.permissions = permissions != null ? EnumSet.copyOf(permissions) : EnumSet.noneOf(RankPermission.class);
    }

    public static GuildRank of(RankId id, RankName name, int priority, Set<RankPermission> permissions) {
        return new GuildRank(id, name, priority, permissions);
    }

    public static GuildRank create(RankName name, int priority, Set<RankPermission> permissions) {
        return new GuildRank(RankId.generate(), name, priority, permissions);
    }

    public static GuildRank reconstitute(RankId id, RankName name, int priority, Set<RankPermission> permissions) {
        return new GuildRank(id, name, priority, permissions);
    }

    public void update(RankName newName, int newPriority, Set<RankPermission> newPermissions) {
        if (newName == null) {
            throw new IllegalArgumentException("RankName cannot be null");
        }
        if (newPriority < 1) {
            throw new IllegalArgumentException("Rank priority must be positive");
        }
        this.name = newName;
        this.priority = newPriority;
        this.permissions.clear();
        if (newPermissions != null) {
            this.permissions.addAll(newPermissions);
        }
    }

    public boolean hasPermission(RankPermission permission) {
        return permissions.contains(permission);
    }

    public RankId getId() {
        return id;
    }

    public RankName getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public Set<RankPermission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GuildRank guildRank = (GuildRank) o;
        return Objects.equals(id, guildRank.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
