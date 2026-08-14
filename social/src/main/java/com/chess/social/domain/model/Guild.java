package com.chess.social.domain.model;

import com.chess.social.domain.exception.CategoryNotFoundException;
import com.chess.social.domain.exception.DuplicateCategoryException;
import com.chess.social.domain.exception.GuildMemberAlreadyExistsException;
import com.chess.social.domain.exception.GuildMemberNotFoundException;
import com.chess.social.domain.exception.RankNotFoundException;
import com.chess.social.domain.exception.UnauthorizedGuildOperationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class Guild {
    private final GuildId id;
    private GuildName name;
    private String description;
    private UserId ownerId;
    private Avatar avatar;
    private final List<GuildCategory> categories;
    private final List<GuildMember> members;
    private final Instant createdAt;
    private Instant updatedAt;

    private Guild(
            GuildId id,
            GuildName name,
            String description,
            UserId ownerId,
            Avatar avatar,
            List<GuildCategory> categories,
            List<GuildMember> members,
            Instant createdAt,
            Instant updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("GuildId cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("GuildName cannot be null");
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner UserId cannot be null");
        }
        if (createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("Timestamps cannot be null");
        }

        this.id = id;
        this.name = name;
        this.description = description != null ? description : "";
        this.ownerId = ownerId;
        this.avatar = avatar != null ? avatar : Avatar.defaultAvatar();
        this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
        this.members = members != null ? new ArrayList<>(members) : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Guild create(GuildName name, String description, UserId creatorId) {
        return create(name, description, creatorId, Avatar.defaultAvatar());
    }

    public static Guild create(GuildName name, String description, UserId creatorId, Avatar avatar) {
        Instant now = Instant.now();
        GuildId guildId = GuildId.generate();

        // Create default category & rank
        GuildCategory defaultCategory = GuildCategory.create(
                CategoryName.of("General"),
                "Default Guild Category"
        );

        Set<RankPermission> leaderPerms = EnumSet.allOf(RankPermission.class);
        GuildRank leaderRank = defaultCategory.addRank(RankName.of("Leader"), 1, leaderPerms);
        defaultCategory.addRank(RankName.of("Member"), 10, EnumSet.of(RankPermission.CHAT_ACCESS));

        // Create initial owner member
        GuildMember ownerMember = GuildMember.create(creatorId, GuildRole.OWNER, leaderRank.getId());

        List<GuildCategory> initialCategories = new ArrayList<>();
        initialCategories.add(defaultCategory);

        List<GuildMember> initialMembers = new ArrayList<>();
        initialMembers.add(ownerMember);

        return new Guild(guildId, name, description, creatorId, avatar, initialCategories, initialMembers, now, now);
    }

    public static Guild reconstitute(
            GuildId id,
            GuildName name,
            String description,
            UserId ownerId,
            Avatar avatar,
            List<GuildCategory> categories,
            List<GuildMember> members,
            Instant createdAt,
            Instant updatedAt) {
        return new Guild(id, name, description, ownerId, avatar, categories, members, createdAt, updatedAt);
    }

    public void updateAvatar(UserId actorId, Avatar newAvatar) {
        verifyAuthorizedForManagement(actorId);
        if (newAvatar == null) {
            throw new IllegalArgumentException("Avatar cannot be null");
        }
        this.avatar = newAvatar;
        this.updatedAt = Instant.now();
    }

    public void transferOwnership(UserId actorId, UserId newOwnerId) {
        verifyOwner(actorId);

        if (actorId.equals(newOwnerId)) {
            return;
        }

        GuildMember newOwnerMember = findMember(newOwnerId)
                .orElseThrow(() -> new GuildMemberNotFoundException("New owner must be a member of the guild"));

        GuildMember currentOwnerMember = findMember(ownerId)
                .orElseThrow(() -> new GuildMemberNotFoundException("Current owner record not found"));

        currentOwnerMember.updateRole(GuildRole.OFFICER);
        newOwnerMember.updateRole(GuildRole.OWNER);

        this.ownerId = newOwnerId;
        this.updatedAt = Instant.now();
    }

    public GuildCategory addCategory(UserId actorId, CategoryName categoryName, String description) {
        verifyAuthorizedForManagement(actorId);

        boolean exists = categories.stream().anyMatch(c -> c.getName().equals(categoryName));
        if (exists) {
            throw new DuplicateCategoryException("Category with name '" + categoryName.getValue() + "' already exists in guild");
        }

        GuildCategory category = GuildCategory.create(categoryName, description);
        categories.add(category);
        this.updatedAt = Instant.now();
        return category;
    }

    public void removeCategory(UserId actorId, CategoryId categoryId) {
        verifyAuthorizedForManagement(actorId);

        if (categories.size() <= 1) {
            throw new UnauthorizedGuildOperationException("Cannot delete the last category of a guild");
        }

        GuildCategory category = findCategory(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));

        categories.remove(category);
        this.updatedAt = Instant.now();
    }

    public GuildRank addRankToCategory(
            UserId actorId,
            CategoryId categoryId,
            RankName rankName,
            int priority,
            Set<RankPermission> permissions) {
        verifyAuthorizedForManagement(actorId);

        GuildCategory category = findCategory(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));

        GuildRank rank = category.addRank(rankName, priority, permissions);
        this.updatedAt = Instant.now();
        return rank;
    }

    public void removeRankFromCategory(UserId actorId, CategoryId categoryId, RankId rankId) {
        verifyAuthorizedForManagement(actorId);

        GuildCategory category = findCategory(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));

        category.removeRank(rankId);
        this.updatedAt = Instant.now();
    }

    public GuildMember addMember(UserId actorId, UserId newMemberId, RankId initialRankId) {
        if (isMember(newMemberId)) {
            throw new GuildMemberAlreadyExistsException("User " + newMemberId + " is already a member of guild");
        }

        if (initialRankId != null) {
            findRank(initialRankId)
                    .orElseThrow(() -> new RankNotFoundException("Initial rank with ID " + initialRankId + " does not exist in guild"));
        }

        GuildMember newMember = GuildMember.create(newMemberId, GuildRole.MEMBER, initialRankId);
        members.add(newMember);
        this.updatedAt = Instant.now();
        return newMember;
    }

    public void removeMember(UserId actorId, UserId targetMemberId) {
        if (ownerId.equals(targetMemberId)) {
            throw new UnauthorizedGuildOperationException("Cannot remove the Guild Owner. Transfer ownership first.");
        }

        GuildMember target = findMember(targetMemberId)
                .orElseThrow(() -> new GuildMemberNotFoundException("Target user is not a member of this guild"));

        if (!actorId.equals(targetMemberId)) {
            verifyCanKick(actorId);
        }

        members.remove(target);
        this.updatedAt = Instant.now();
    }

    public void assignRankToMember(UserId actorId, UserId targetMemberId, RankId newRankId) {
        verifyAuthorizedForManagement(actorId);

        GuildMember target = findMember(targetMemberId)
                .orElseThrow(() -> new GuildMemberNotFoundException("Target user is not a member of this guild"));

        if (newRankId != null) {
            findRank(newRankId)
                    .orElseThrow(() -> new RankNotFoundException("Rank with ID " + newRankId + " does not exist in guild"));
        }

        target.assignRank(newRankId);
        this.updatedAt = Instant.now();
    }

    public Optional<GuildCategory> findCategory(CategoryId categoryId) {
        return categories.stream().filter(c -> c.getId().equals(categoryId)).findFirst();
    }

    public Optional<GuildRank> findRank(RankId rankId) {
        for (GuildCategory cat : categories) {
            Optional<GuildRank> rankOpt = cat.findRank(rankId);
            if (rankOpt.isPresent()) {
                return rankOpt;
            }
        }
        return Optional.empty();
    }

    public Optional<GuildMember> findMember(UserId userId) {
        return members.stream().filter(m -> m.getUserId().equals(userId)).findFirst();
    }

    public boolean isMember(UserId userId) {
        return members.stream().anyMatch(m -> m.getUserId().equals(userId));
    }

    public boolean isOwner(UserId userId) {
        return ownerId.equals(userId);
    }

    private void verifyOwner(UserId actorId) {
        if (!isOwner(actorId)) {
            throw new UnauthorizedGuildOperationException("Operation requires Guild Owner privileges");
        }
    }

    private void verifyAuthorizedForManagement(UserId actorId) {
        GuildMember actor = findMember(actorId)
                .orElseThrow(() -> new UnauthorizedGuildOperationException("Actor is not a member of this guild"));

        if (actor.getRole() == GuildRole.OWNER || actor.getRole() == GuildRole.OFFICER) {
            return;
        }

        if (actor.getAssignedRankId() != null) {
            GuildRank rank = findRank(actor.getAssignedRankId()).orElse(null);
            if (rank != null && (rank.hasPermission(RankPermission.MANAGE_CATEGORIES) || rank.hasPermission(RankPermission.MANAGE_RANKS))) {
                return;
            }
        }

        throw new UnauthorizedGuildOperationException("Actor does not have management permissions in this guild");
    }

    private void verifyCanKick(UserId actorId) {
        if (hasPermission(actorId, RankPermission.KICK_MEMBERS)) {
            return;
        }
        throw new UnauthorizedGuildOperationException("Actor does not have permission to kick members from this guild");
    }

    public boolean hasPermission(UserId userId, RankPermission permission) {
        GuildMember member = findMember(userId).orElse(null);
        if (member == null) {
            return false;
        }
        if (member.getRole() == GuildRole.OWNER || member.getRole() == GuildRole.OFFICER) {
            return true;
        }
        if (member.getAssignedRankId() != null) {
            GuildRank rank = findRank(member.getAssignedRankId()).orElse(null);
            if (rank != null && rank.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    public GuildId getId() {
        return id;
    }

    public GuildName getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UserId getOwnerId() {
        return ownerId;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public List<GuildCategory> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    public List<GuildMember> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guild guild = (Guild) o;
        return Objects.equals(id, guild.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
