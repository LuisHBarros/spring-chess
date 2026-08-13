package com.chess.social.domain;

import com.chess.social.domain.model.GuildId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GuildIdTest {

    @Test
    @DisplayName("Should generate and wrap GuildId")
    void shouldCreateGuildId() {
        GuildId id = GuildId.generate();
        assertThat(id.getValue()).isNotNull();

        UUID uuid = UUID.randomUUID();
        GuildId fromUuid = GuildId.from(uuid);
        assertThat(fromUuid.getValue()).isEqualTo(uuid);
    }
}
