package com.chess.game.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameIdTest {

    @Test
    @DisplayName("Should generate random GameId and reconstitute from UUID")
    void shouldGenerateAndReconstituteGameId() {
        GameId generated = GameId.generate();
        assertThat(generated.getValue()).isNotNull();

        UUID uuid = UUID.randomUUID();
        GameId fromUuid = GameId.from(uuid);
        assertThat(fromUuid.getValue()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("Should throw exception when creating GameId from null")
    void shouldThrowForNullUuid() {
        assertThatThrownBy(() -> GameId.from((UUID) null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
