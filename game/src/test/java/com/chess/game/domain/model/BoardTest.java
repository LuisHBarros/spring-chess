package com.chess.game.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BoardTest {

    @Test
    @DisplayName("Should initialize standard chess board with 32 pieces")
    void shouldInitializeStandardBoard() {
        Board board = Board.create();

        assertThat(board.getAllPieces()).hasSize(32);
        assertThat(board.getAllPieces(Color.WHITE)).hasSize(16);
        assertThat(board.getAllPieces(Color.BLACK)).hasSize(16);

        assertThat(board.getKingPosition(Color.WHITE)).isEqualTo(Position.fromAlgebraic("e1"));
        assertThat(board.getKingPosition(Color.BLACK)).isEqualTo(Position.fromAlgebraic("e8"));

        Optional<Piece> e2Pawn = board.getPieceAt(Position.fromAlgebraic("e2"));
        assertThat(e2Pawn).isPresent();
        assertThat(e2Pawn.get().getPieceType()).isEqualTo(PieceType.PAWN);
        assertThat(e2Pawn.get().getColor()).isEqualTo(Color.WHITE);
    }

    @Test
    @DisplayName("Should correctly identify attacked squares")
    void shouldIdentifyAttackedSquares() {
        Board board = Board.create();

        // White pawn on e2 attacks d3 and f3
        assertThat(board.isSquareAttackedBy(Position.fromAlgebraic("d3"), Color.WHITE)).isTrue();
        assertThat(board.isSquareAttackedBy(Position.fromAlgebraic("f3"), Color.WHITE)).isTrue();
        assertThat(board.isSquareAttackedBy(Position.fromAlgebraic("e3"), Color.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Should check clear path between positions")
    void shouldCheckPathClear() {
        Board board = Board.create();

        // Path between e1 (king) and e2 (pawn) is clear of obstacles between them
        assertThat(board.isPathClear(Position.fromAlgebraic("e1"), Position.fromAlgebraic("e2"))).isTrue();

        // Path from e1 to e4 is blocked by e2 pawn
        assertThat(board.isPathClear(Position.fromAlgebraic("e1"), Position.fromAlgebraic("e4"))).isFalse();
    }

    @Test
    @DisplayName("Should move piece on board creating a new board state")
    void shouldMovePieceOnBoard() {
        Board board = Board.create();

        Position from = Position.fromAlgebraic("e2");
        Position to = Position.fromAlgebraic("e4");
        Move move = Move.of(from, to, PieceType.PAWN, MoveType.DOUBLE_PAWN_PUSH);

        Board newBoard = board.movePiece(move);

        assertThat(newBoard.getPieceAt(from)).isEmpty();
        assertThat(newBoard.getPieceAt(to)).isPresent();
        assertThat(newBoard.getPieceAt(to).get().getPieceType()).isEqualTo(PieceType.PAWN);
        assertThat(newBoard.getPieceAt(to).get().hasMoved()).isTrue();
    }
}
