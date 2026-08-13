package com.chess.game.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoveTest {

    @Test
    @DisplayName("Should create move with correct properties")
    void shouldCreateMove() {
        Position from = Position.fromAlgebraic("e2");
        Position to = Position.fromAlgebraic("e4");
        Move move = Move.of(from, to, PieceType.PAWN, MoveType.DOUBLE_PAWN_PUSH);

        assertThat(move.getFrom()).isEqualTo(from);
        assertThat(move.getTo()).isEqualTo(to);
        assertThat(move.getPieceType()).isEqualTo(PieceType.PAWN);
        assertThat(move.getMoveType()).isEqualTo(MoveType.DOUBLE_PAWN_PUSH);
    }

    @Test
    @DisplayName("Should support pawn promotion move")
    void shouldSupportPromotionMove() {
        Position from = Position.fromAlgebraic("e7");
        Position to = Position.fromAlgebraic("e8");
        Move move = Move.ofPromotion(from, to, PieceType.QUEEN);

        assertThat(move.getMoveType()).isEqualTo(MoveType.PAWN_PROMOTION);
        assertThat(move.getPromotionPieceType()).isEqualTo(PieceType.QUEEN);
    }
}
