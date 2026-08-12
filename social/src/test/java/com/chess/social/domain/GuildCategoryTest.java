package com.chess.social.domain;

import com.chess.social.domain.exception.DuplicateRankException;
import com.chess.social.domain.exception.RankNotFoundException;
import com.chess.social.domain.model.CategoryName;
import com.chess.social.domain.model.GuildCategory;
import com.chess.social.domain.model.GuildRank;
import com.chess.social.domain.model.RankId;
import com.chess.social.domain.model.RankName;
import com.chess.social.domain.model.RankPermission;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

class GuildCategoryTest {

    @Test
    @DisplayName("Should create category and add custom ranks")
    void shouldCreateCategoryAndAddRanks() {
        GuildCategory category = GuildCategory.create(CategoryName.of("Competitive"), "For ranked players");

        assertEquals("Competitive", category.getName().getValue());
        assertEquals("For ranked players", category.getDescription());

        GuildRank rank = category.addRank(RankName.of("Captain"), 2, EnumSet.of(RankPermission.INVITE_MEMBERS));

        assertNotNull(rank.getId());
        assertEquals("Captain", rank.getName().getValue());
        assertEquals(2, rank.getPriority());
        assertTrue(rank.hasPermission(RankPermission.INVITE_MEMBERS));
        assertEquals(1, category.getRanks().size());
    }

    @Test
    @DisplayName("Should throw exception when adding duplicate rank name")
    void shouldThrowOnDuplicateRankName() {
        GuildCategory category = GuildCategory.create(CategoryName.of("Tactics"), "Puzzle group");
        category.addRank(RankName.of("Strategist"), 1, EnumSet.noneOf(RankPermission.class));

        assertThrows(DuplicateRankException.class, () ->
                category.addRank(RankName.of("Strategist"), 2, EnumSet.noneOf(RankPermission.class))
        );
    }

    @Test
    @DisplayName("Should remove rank from category")
    void shouldRemoveRankFromCategory() {
        GuildCategory category = GuildCategory.create(CategoryName.of("Events"), "Event management");
        GuildRank rank = category.addRank(RankName.of("Host"), 1, EnumSet.of(RankPermission.POST_ANNOUNCEMENTS));

        category.removeRank(rank.getId());

        assertEquals(0, category.getRanks().size());
        assertTrue(category.findRank(rank.getId()).isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when removing non-existent rank")
    void shouldThrowWhenRemovingNonExistentRank() {
        GuildCategory category = GuildCategory.create(CategoryName.of("Casual"), "Fun group");

        assertThrows(RankNotFoundException.class, () -> category.removeRank(RankId.generate()));
    }
}
