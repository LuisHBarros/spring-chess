package com.chess.social.domain;

import com.chess.social.domain.exception.DuplicateCategoryException;
import com.chess.social.domain.exception.GuildNotFoundException;
import com.chess.social.domain.model.CategoryName;
import com.chess.social.domain.model.Guild;
import com.chess.social.domain.model.GuildCategory;
import com.chess.social.domain.model.GuildMember;
import com.chess.social.domain.model.GuildName;
import com.chess.social.domain.model.GuildRank;
import com.chess.social.domain.model.RankId;
import com.chess.social.domain.model.RankName;
import com.chess.social.domain.model.RankPermission;
import com.chess.social.domain.model.UserId;
import com.chess.social.domain.repository.GuildRepository;
import com.chess.social.domain.service.GuildDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuildDomainServiceTest {

    @Mock
    private GuildRepository guildRepository;

    private GuildDomainService service;

    @BeforeEach
    void setUp() {
        service = new GuildDomainService(guildRepository);
    }

    @Test
    @DisplayName("Should successfully create guild when name is unique")
    void shouldCreateGuildWhenNameIsUnique() {
        GuildName name = GuildName.of("Unique Guild");
        UserId creator = UserId.generate();

        when(guildRepository.existsByName(name)).thenReturn(false);
        when(guildRepository.save(any(Guild.class))).thenAnswer(inv -> inv.getArgument(0));

        Guild guild = service.createGuild(name, "Description", creator);

        assertNotNull(guild);
        assertEquals(name, guild.getName());
        assertEquals(creator, guild.getOwnerId());
        verify(guildRepository).save(any(Guild.class));
    }

    @Test
    @DisplayName("Should throw exception when guild name already exists")
    void shouldThrowWhenGuildNameExists() {
        GuildName name = GuildName.of("Existing Guild");
        UserId creator = UserId.generate();

        when(guildRepository.existsByName(name)).thenReturn(true);

        assertThrows(DuplicateCategoryException.class, () ->
                service.createGuild(name, "Description", creator)
        );
        verify(guildRepository, never()).save(any(Guild.class));
    }

    @Test
    @DisplayName("Should transfer ownership via service")
    void shouldTransferOwnership() {
        UserId owner = UserId.generate();
        UserId newOwner = UserId.generate();
        Guild guild = Guild.create(GuildName.of("Test Guild"), "Desc", owner);
        guild.addMember(owner, newOwner, null);

        when(guildRepository.findById(guild.getId())).thenReturn(Optional.of(guild));
        when(guildRepository.save(any(Guild.class))).thenAnswer(inv -> inv.getArgument(0));

        Guild result = service.transferOwnership(owner, guild.getId(), newOwner);

        assertEquals(newOwner, result.getOwnerId());
        verify(guildRepository).save(guild);
    }

    @Test
    @DisplayName("Should add category and rank via service")
    void shouldAddCategoryAndRank() {
        UserId owner = UserId.generate();
        Guild guild = Guild.create(GuildName.of("Test Guild"), "Desc", owner);

        when(guildRepository.findById(guild.getId())).thenReturn(Optional.of(guild));
        when(guildRepository.save(any(Guild.class))).thenAnswer(inv -> inv.getArgument(0));

        GuildCategory category = service.addCategory(owner, guild.getId(), CategoryName.of("Strategy"), "Desc");
        assertNotNull(category);
        verify(guildRepository).save(guild);

        GuildRank rank = service.addRankToCategory(
                owner,
                guild.getId(),
                category.getId(),
                RankName.of("Mastermind"),
                1,
                EnumSet.of(RankPermission.MANAGE_RANKS)
        );

        assertNotNull(rank);
        verify(guildRepository, times(2)).save(guild);
    }

    @Test
    @DisplayName("Should add and remove member via service")
    void shouldAddAndRemoveMember() {
        UserId owner = UserId.generate();
        UserId newMember = UserId.generate();
        Guild guild = Guild.create(GuildName.of("Test Guild"), "Desc", owner);

        when(guildRepository.findById(guild.getId())).thenReturn(Optional.of(guild));
        when(guildRepository.save(any(Guild.class))).thenAnswer(inv -> inv.getArgument(0));

        GuildMember member = service.addMember(owner, guild.getId(), newMember, null);
        assertNotNull(member);
        assertEquals(newMember, member.getUserId());

        service.removeMember(owner, guild.getId(), newMember);
        assertFalse(guild.isMember(newMember));
    }

    @Test
    @DisplayName("Should delete guild via service")
    void shouldDeleteGuild() {
        UserId owner = UserId.generate();
        Guild guild = Guild.create(GuildName.of("Test Guild"), "Desc", owner);

        when(guildRepository.findById(guild.getId())).thenReturn(Optional.of(guild));

        service.deleteGuild(owner, guild.getId());

        verify(guildRepository).delete(guild);
    }
}
