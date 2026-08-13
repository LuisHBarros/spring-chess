package com.chess.social.domain;

import com.chess.social.domain.model.GuildRank;
import com.chess.social.domain.model.RankId;
import com.chess.social.domain.model.RankName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GuildRankTest {

    @Test
    @DisplayName("Should create GuildRank with permissions and priority")
    void shouldCreateGuildRank() {
        RankId id = RankId.from(UUID.randomUUID());
        RankName name = RankName.of("Officer");
        Set<String> permissions = Set.of("INVITE_MEMBERS", "KICK_MEMBERS");

        GuildRank rank = GuildRank.of(id, name, 10, permissions);

        assertThat(rank.getId()).isEqualTo(id);
        assertThat(rank.getName()).isEqualTo(name);
        assertThat(rank.getPriority()).isEqualTo(10);
        assertThat(rank.getPermissions()).contains("INVITE_MEMBERS");
    }
}
