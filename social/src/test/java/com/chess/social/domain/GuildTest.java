package com.chess.social.domain;

import com.chess.social.domain.exception.CategoryNotFoundException;
import com.chess.social.domain.exception.DuplicateCategoryException;
import com.chess.social.domain.exception.GuildMemberAlreadyExistsException;
import com.chess.social.domain.exception.GuildMemberNotFoundException;
import com.chess.social.domain.exception.UnauthorizedGuildOperationException;
import com.chess.social.domain.model.Avatar;
import com.chess.social.domain.model.CategoryId;
import com.chess.social.domain.model.CategoryName;
import com.chess.social.domain.model.Guild;
import com.chess.social.domain.model.GuildCategory;
import com.chess.social.domain.model.GuildMember;
import com.chess.social.domain.model.GuildName;
import com.chess.social.domain.model.GuildRank;
import com.chess.social.domain.model.GuildRole;
import com.chess.social.domain.model.RankName;
import com.chess.social.domain.model.RankPermission;
import com.chess.social.domain.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class GuildTest {

    @Test
    @DisplayName("Should successfully create a Guild with owner, default category, and default ranks")
    void shouldCreateGuild() {
        UserId ownerId = UserId.generate();
        GuildName name = GuildName.of("Grandmasters Club");

        Guild guild = Guild.create(name, "Elite chess guild", ownerId);

        assertNotNull(guild.getId());
        assertEquals("Grandmasters Club", guild.getName().getValue());
        assertEquals("Elite chess guild", guild.getDescription());
        assertEquals(ownerId, guild.getOwnerId());
        assertTrue(guild.isOwner(ownerId));
        assertTrue(guild.isMember(ownerId));

        // Default category & members check
        assertEquals(1, guild.getCategories().size());
        assertEquals("General", guild.getCategories().get(0).getName().getValue());
        assertEquals(1, guild.getMembers().size());
        assertEquals(GuildRole.OWNER, guild.getMembers().get(0).getRole());
    }

    @Test
    @DisplayName("Should allow owner to transfer guild ownership to another member")
    void shouldTransferOwnership() {
        UserId ownerId = UserId.generate();
        UserId newOwnerId = UserId.generate();
        Guild guild = Guild.create(GuildName.of("Strategy Guild"), "Desc", ownerId);

        // Add new member
        guild.addMember(ownerId, newOwnerId, null);

        // Transfer ownership
        guild.transferOwnership(ownerId, newOwnerId);

        assertEquals(newOwnerId, guild.getOwnerId());
        assertTrue(guild.isOwner(newOwnerId));
        assertFalse(guild.isOwner(ownerId));

        GuildMember oldOwnerMember = guild.findMember(ownerId).orElseThrow();
        GuildMember newOwnerMember = guild.findMember(newOwnerId).orElseThrow();
        assertEquals(GuildRole.OFFICER, oldOwnerMember.getRole());
        assertEquals(GuildRole.OWNER, newOwnerMember.getRole());
    }

    @Test
    @DisplayName("Should throw exception if non-owner tries to transfer ownership")
    void shouldThrowIfNonOwnerTransfersOwnership() {
        UserId ownerId = UserId.generate();
        UserId memberId = UserId.generate();
        Guild guild = Guild.create(GuildName.of("Knights Templar"), "Desc", ownerId);
        guild.addMember(ownerId, memberId, null);

        assertThrows(UnauthorizedGuildOperationException.class, () ->
                guild.transferOwnership(memberId, memberId)
        );
    }

    @Test
    @DisplayName("Should add custom category and ranks")
    void shouldAddCategoryAndRanks() {
        UserId ownerId = UserId.generate();
        Guild guild = Guild.create(GuildName.of("Rookies"), "Desc", ownerId);

        GuildCategory cat = guild.addCategory(ownerId, CategoryName.of("Blitz Tournaments"), "Speed chess");
        assertNotNull(cat.getId());
        assertEquals(2, guild.getCategories().size());

        GuildRank rank = guild.addRankToCategory(
                ownerId,
                cat.getId(),
                RankName.of("Speed Demon"),
                2,
                EnumSet.of(RankPermission.INVITE_MEMBERS)
        );

        assertNotNull(rank.getId());
        assertTrue(guild.findRank(rank.getId()).isPresent());
    }

    @Test
    @DisplayName("Should throw exception when adding duplicate category name")
    void shouldThrowOnDuplicateCategoryName() {
        UserId ownerId = UserId.generate();
        Guild guild = Guild.create(GuildName.of("Pawns United"), "Desc", ownerId);

        assertThrows(DuplicateCategoryException.class, () ->
                guild.addCategory(ownerId, CategoryName.of("General"), "Duplicate category")
        );
    }

    @Test
    @DisplayName("Should allow adding member and assigning customized rank")
    void shouldAddMemberAndAssignRank() {
        UserId ownerId = UserId.generate();
        UserId memberId = UserId.generate();
        Guild guild = Guild.create(GuildName.of("Endgame Masters"), "Desc", ownerId);

        GuildCategory generalCat = guild.getCategories().get(0);
        GuildRank memberRank = generalCat.addRank(RankName.of("Apprentice"), 5, EnumSet.of(RankPermission.CHAT_ACCESS));

        GuildMember member = guild.addMember(ownerId, memberId, memberRank.getId());

        assertEquals(memberId, member.getUserId());
        assertEquals(memberRank.getId(), member.getAssignedRankId());
        assertEquals(2, guild.getMembers().size());

        // Assign new rank
        GuildRank higherRank = generalCat.addRank(RankName.of("Veteran"), 3, EnumSet.of(RankPermission.INVITE_MEMBERS));
        guild.assignRankToMember(ownerId, memberId, higherRank.getId());

        assertEquals(higherRank.getId(), guild.findMember(memberId).orElseThrow().getAssignedRankId());
    }

    @Test
    @DisplayName("Should throw exception when adding existing member")
    void shouldThrowOnDuplicateMember() {
        UserId ownerId = UserId.generate();
        Guild guild = Guild.create(GuildName.of("Bishop Guild"), "Desc", ownerId);

        assertThrows(GuildMemberAlreadyExistsException.class, () ->
                guild.addMember(ownerId, ownerId, null)
        );
    }

    @Test
    @DisplayName("Should remove member from guild")
    void shouldRemoveMember() {
        UserId ownerId = UserId.generate();
        UserId memberId = UserId.generate();
        Guild guild = Guild.create(GuildName.of("King's Gambit"), "Desc", ownerId);
        guild.addMember(ownerId, memberId, null);

        guild.removeMember(ownerId, memberId);

        assertFalse(guild.isMember(memberId));
        assertEquals(1, guild.getMembers().size());
    }

    @Test
    @DisplayName("Should throw exception when attempting to remove the owner")
    void shouldThrowWhenRemovingOwner() {
        UserId ownerId = UserId.generate();
        Guild guild = Guild.create(GuildName.of("Queens of Chess"), "Desc", ownerId);

        assertThrows(UnauthorizedGuildOperationException.class, () ->
                guild.removeMember(ownerId, ownerId)
        );
    }

    @Test
    @DisplayName("Should allow authorized manager to update guild avatar")
    void shouldUpdateGuildAvatar() {
        UserId ownerId = UserId.generate();
        Guild guild = Guild.create(GuildName.of("Avatar Guild"), "Desc", ownerId);

        Avatar newAvatar = Avatar.of("https://cdn.chess.com/guilds/avatar.png");
        guild.updateAvatar(ownerId, newAvatar);

        assertEquals(newAvatar, guild.getAvatar());
    }

    @Test
    @DisplayName("Should throw exception when non-manager attempts to update guild avatar")
    void shouldThrowWhenNonManagerUpdatesGuildAvatar() {
        UserId ownerId = UserId.generate();
        UserId memberId = UserId.generate();
        Guild guild = Guild.create(GuildName.of("Protected Guild"), "Desc", ownerId);
        guild.addMember(ownerId, memberId, null);

        Avatar newAvatar = Avatar.of("https://cdn.chess.com/guilds/hacked.png");
        assertThrows(UnauthorizedGuildOperationException.class, () ->
                guild.updateAvatar(memberId, newAvatar)
        );
    }
}

