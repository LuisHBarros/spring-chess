package com.chess.social.domain;

import com.chess.social.domain.model.GuildMember;
import com.chess.social.domain.model.RankId;
import com.chess.social.domain.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GuildMemberTest {

    @Test
    @DisplayName("Should create GuildMember and update rank")
    void shouldCreateGuildMember() {
        UserId userId = UserId.from(UUID.randomUUID());
        RankId rankId = RankId.from(UUID.randomUUID());
        Instant joinedAt = Instant.now();

        GuildMember member = GuildMember.of(userId, rankId, joinedAt);

        assertThat(member.getUserId()).isEqualTo(userId);
        assertThat(member.getRankId()).isEqualTo(rankId);
        assertThat(member.getJoinedAt()).isEqualTo(joinedAt);

        RankId newRank = RankId.from(UUID.randomUUID());
        GuildMember updated = member.withRank(newRank);

        assertThat(updated.getRankId()).isEqualTo(newRank);
    }
}
