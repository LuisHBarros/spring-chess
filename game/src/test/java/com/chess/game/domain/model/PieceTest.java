package com.chess.game.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PieceTest {

    @Test
    @DisplayName("Should create piece and update position on move")
    void shouldCreatePieceAndMove() {
        Position e2 = Position.fromAlgebraic("e2");
        Position e4 = Position.fromAlgebraic("e4");
        Piece pawn = Piece.create(PieceType.PAWN, Color.WHITE, e2);

        assertThat(pawn.getPieceType()).isEqualTo(PieceType.PAWN);
        assertThat(pawn.getColor()).isEqualTo(Color.WHITE);
        assertThat(pawn.getPosition()).isEqualTo(e2);
        assertThat(pawn.hasMoved()).isFalse();

        Piece movedPawn = pawn.moveTo(e4);

        assertThat(movedPawn.getPosition()).isEqualTo(e4);
        assertThat(movedPawn.hasMoved()).isTrue();
    }
}
