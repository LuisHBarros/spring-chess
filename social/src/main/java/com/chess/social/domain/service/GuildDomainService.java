package com.chess.social.domain.service;

import com.chess.social.domain.exception.DuplicateCategoryException;
import com.chess.social.domain.exception.GuildNotFoundException;
import com.chess.social.domain.model.Avatar;
import com.chess.social.domain.model.CategoryId;
import com.chess.social.domain.model.CategoryName;
import com.chess.social.domain.model.Guild;
import com.chess.social.domain.model.GuildCategory;
import com.chess.social.domain.model.GuildId;
import com.chess.social.domain.model.GuildMember;
import com.chess.social.domain.model.GuildName;
import com.chess.social.domain.model.GuildRank;
import com.chess.social.domain.model.RankId;
import com.chess.social.domain.model.RankName;
import com.chess.social.domain.model.RankPermission;
import com.chess.social.domain.model.UserId;
import com.chess.social.domain.repository.GuildRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Transactional
public class GuildDomainService {
    private final GuildRepository guildRepository;

    public GuildDomainService(GuildRepository guildRepository) {
        if (guildRepository == null) {
            throw new IllegalArgumentException("GuildRepository cannot be null");
        }
        this.guildRepository = guildRepository;
    }

    public Guild createGuild(GuildName name, String description, UserId creatorId) {
        return createGuild(name, description, creatorId, Avatar.defaultAvatar());
    }

    public Guild createGuild(GuildName name, String description, UserId creatorId, Avatar avatar) {
        if (guildRepository.existsByName(name)) {
            throw new DuplicateCategoryException("A guild with name '" + name.getValue() + "' already exists");
        }

        Guild guild = Guild.create(name, description, creatorId, avatar);
        try {
            return guildRepository.save(guild);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateCategoryException("A guild with name '" + name.getValue() + "' already exists");
        }
    }

    public Guild updateGuildAvatar(UserId actorId, GuildId guildId, Avatar newAvatar) {
        Guild guild = getGuildOrThrow(guildId);
        guild.updateAvatar(actorId, newAvatar);
        return guildRepository.save(guild);
    }

    public Guild transferOwnership(UserId actorId, GuildId guildId, UserId newOwnerId) {
        Guild guild = getGuildOrThrow(guildId);
        guild.transferOwnership(actorId, newOwnerId);
        return guildRepository.save(guild);
    }

    public GuildCategory addCategory(UserId actorId, GuildId guildId, CategoryName name, String description) {
        Guild guild = getGuildOrThrow(guildId);
        GuildCategory category = guild.addCategory(actorId, name, description);
        guildRepository.save(guild);
        return category;
    }

    public void removeCategory(UserId actorId, GuildId guildId, CategoryId categoryId) {
        Guild guild = getGuildOrThrow(guildId);
        guild.removeCategory(actorId, categoryId);
        guildRepository.save(guild);
    }

    public GuildRank addRankToCategory(
            UserId actorId,
            GuildId guildId,
            CategoryId categoryId,
            RankName name,
            int priority,
            Set<RankPermission> permissions) {
        Guild guild = getGuildOrThrow(guildId);
        GuildRank rank = guild.addRankToCategory(actorId, categoryId, name, priority, permissions);
        guildRepository.save(guild);
        return rank;
    }

    public void removeRankFromCategory(UserId actorId, GuildId guildId, CategoryId categoryId, RankId rankId) {
        Guild guild = getGuildOrThrow(guildId);
        guild.removeRankFromCategory(actorId, categoryId, rankId);
        guildRepository.save(guild);
    }

    public GuildMember addMember(UserId actorId, GuildId guildId, UserId newMemberId, RankId initialRankId) {
        Guild guild = getGuildOrThrow(guildId);
        GuildMember member = guild.addMember(actorId, newMemberId, initialRankId);
        guildRepository.save(guild);
        return member;
    }

    public void removeMember(UserId actorId, GuildId guildId, UserId targetMemberId) {
        Guild guild = getGuildOrThrow(guildId);
        guild.removeMember(actorId, targetMemberId);
        guildRepository.save(guild);
    }

    public void assignRankToMember(UserId actorId, GuildId guildId, UserId targetMemberId, RankId rankId) {
        Guild guild = getGuildOrThrow(guildId);
        guild.assignRankToMember(actorId, targetMemberId, rankId);
        guildRepository.save(guild);
    }

    public void deleteGuild(UserId actorId, GuildId guildId) {
        Guild guild = getGuildOrThrow(guildId);
        guild.transferOwnership(actorId, actorId); // Verifies actorId is owner
        guildRepository.delete(guild);
    }

    public boolean hasPermission(UserId userId, GuildId guildId, RankPermission permission) {
        Guild guild = getGuildOrThrow(guildId);
        return guild.hasPermission(userId, permission);
    }

    private Guild getGuildOrThrow(GuildId guildId) {
        return guildRepository.findById(guildId)
                .orElseThrow(() -> new GuildNotFoundException("Guild not found with ID: " + guildId));
    }
}
