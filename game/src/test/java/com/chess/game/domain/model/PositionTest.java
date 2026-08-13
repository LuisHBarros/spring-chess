package com.chess.game.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PositionTest {

    @Test
    @DisplayName("Should create position from file and rank coordinates")
    void shouldCreatePositionFromCoordinates() {
        Position pos = Position.of(4, 3);

        assertThat(pos.getFile()).isEqualTo(4);
        assertThat(pos.getRank()).isEqualTo(3);
        assertThat(pos.toAlgebraic()).isEqualTo("e4");
    }

    @Test
    @DisplayName("Should parse position from algebraic notation")
    void shouldParseFromAlgebraicNotation() {
        Position pos = Position.fromAlgebraic("a1");
        assertThat(pos.getFile()).isEqualTo(0);
        assertThat(pos.getRank()).isEqualTo(0);

        Position posH8 = Position.fromAlgebraic("h8");
        assertThat(posH8.getFile()).isEqualTo(7);
        assertThat(posH8.getRank()).isEqualTo(7);
    }

    @Test
    @DisplayName("Should throw exception for out-of-bounds coordinates")
    void shouldThrowForOutOfBoundsCoordinates() {
        assertThatThrownBy(() -> Position.of(-1, 0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Position.of(8, 0))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Position.fromAlgebraic("z9"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should verify equality and hashcode")
    void shouldVerifyEquality() {
        Position p1 = Position.of(3, 3);
        Position p2 = Position.fromAlgebraic("d4");

        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }
}
