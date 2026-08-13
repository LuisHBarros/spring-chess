package com.chess.game.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlayerIdTest {

    @Test
    @DisplayName("Should create PlayerId from valid UUID")
    void shouldCreatePlayerId() {
        UUID uuid = UUID.randomUUID();
        PlayerId playerId = PlayerId.from(uuid);

        assertThat(playerId.getValue()).isEqualTo(uuid);
        assertThat(playerId.toString()).isEqualTo(uuid.toString());
    }

    @Test
    @DisplayName("Should throw exception when creating PlayerId from null")
    void shouldThrowForNullUuid() {
        assertThatThrownBy(() -> PlayerId.from((UUID) null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
