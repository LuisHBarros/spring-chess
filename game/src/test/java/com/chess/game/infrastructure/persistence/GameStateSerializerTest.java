package com.chess.game.infrastructure.persistence;

import com.chess.game.domain.model.Board;
import com.chess.game.domain.model.Color;
import com.chess.game.domain.model.Game;
import com.chess.game.domain.model.Move;
import com.chess.game.domain.model.MoveType;
import com.chess.game.domain.model.Piece;
import com.chess.game.domain.model.PieceType;
import com.chess.game.domain.model.PlayerId;
import com.chess.game.domain.model.Position;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GameStateSerializerTest {

    @Test
    @DisplayName("Initial board round-trips through serialize/deserialize with hasMoved preserved")
    void initialBoardRoundTrip() {
        Board board = Board.create();
        String json = GameStateSerializer.serializeBoard(board);

        Board restored = GameStateSerializer.deserializeBoard(json);

        assertThat(restored.getPieces()).hasSameSizeAs(board.getPieces());
        assertThat(restored.getAllPieces()).allSatisfy(piece ->
                assertThat(board.getPieceAt(piece.getPosition()))
                        .as("piece at %s", piece.getPosition())
                        .isPresent()
                        .get()
                        .usingRecursiveComparison()
                        .isEqualTo(piece));
    }

    @Test
    @DisplayName("Board after several moves round-trips, preserving hasMoved for castling eligibility")
    void boardAfterMovesRoundTripPreservesHasMoved() {
        PlayerId white = PlayerId.generate();
        PlayerId black = PlayerId.generate();
        Game game = Game.create(white, black, 600, 5);
        game.start();
        game.makeMove(white, Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"), null);
        game.makeMove(black, Position.fromAlgebraic("e7"), Position.fromAlgebraic("e5"), null);
        game.makeMove(white, Position.fromAlgebraic("g1"), Position.fromAlgebraic("f3"), null);

        Board board = game.getBoard();
        String json = GameStateSerializer.serializeBoard(board);
        Board restored = GameStateSerializer.deserializeBoard(json);

        assertThat(restored.getPieces()).hasSameSizeAs(board.getPieces());
        // The white king-side rook (h1) is still unmoved; the knight that moved to f3 is moved.
        assertThat(restored.getPieceAt(Position.fromAlgebraic("h1")))
                .isPresent().get().extracting(Piece::hasMoved).isEqualTo(false);
        assertThat(restored.getPieceAt(Position.fromAlgebraic("f3")))
                .isPresent().get().extracting(Piece::hasMoved).isEqualTo(true);
        assertThat(restored.getPieceAt(Position.fromAlgebraic("e4")))
                .isPresent().get().extracting(Piece::hasMoved).isEqualTo(true);
    }

    @Test
    @DisplayName("Move history round-trips through serialize/deserialize including special moves")
    void moveHistoryRoundTrip() {
        List<Move> moves = List.of(
                Move.of(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"), PieceType.PAWN, MoveType.DOUBLE_PAWN_PUSH),
                Move.capture(Position.fromAlgebraic("g1"), Position.fromAlgebraic("f3"), PieceType.KNIGHT, PieceType.PAWN),
                Move.castlingKingside(Position.fromAlgebraic("e1"), Position.fromAlgebraic("g1")),
                Move.promotion(Position.fromAlgebraic("e7"), Position.fromAlgebraic("e8"), PieceType.QUEEN, null));

        String json = GameStateSerializer.serializeMoves(moves);
        List<Move> restored = GameStateSerializer.deserializeMoves(json);

        assertThat(restored).hasSize(moves.size());
        for (int i = 0; i < moves.size(); i++) {
            Move original = moves.get(i);
            Move r = restored.get(i);
            assertThat(r.getFrom()).isEqualTo(original.getFrom());
            assertThat(r.getTo()).isEqualTo(original.getTo());
            assertThat(r.getPieceType()).isEqualTo(original.getPieceType());
            assertThat(r.getMoveType()).isEqualTo(original.getMoveType());
            assertThat(r.getCapturedPieceType()).isEqualTo(original.getCapturedPieceType());
            assertThat(r.getPromotionPieceType()).isEqualTo(original.getPromotionPieceType());
        }
    }

    @Test
    @DisplayName("Legacy boardJson '{}' or blank falls back to the initial position")
    void legacyBoardJsonFallsBackToInitialBoard() {
        Board fromEmptyObject = GameStateSerializer.deserializeBoard("{}");
        Board fromBlank = GameStateSerializer.deserializeBoard("");
        Board fromNull = GameStateSerializer.deserializeBoard(null);

        assertThat(fromEmptyObject.getPieces()).hasSameSizeAs(Board.create().getPieces());
        assertThat(fromBlank.getPieces()).hasSameSizeAs(Board.create().getPieces());
        assertThat(fromNull.getPieces()).hasSameSizeAs(Board.create().getPieces());
    }

    @Test
    @DisplayName("Legacy movesJson '[]' or blank yields an empty move list")
    void legacyMovesJsonYieldsEmptyList() {
        assertThat(GameStateSerializer.deserializeMoves("[]")).isEmpty();
        assertThat(GameStateSerializer.deserializeMoves("")).isEmpty();
        assertThat(GameStateSerializer.deserializeMoves(null)).isEmpty();
    }

    @Test
    @DisplayName("Round-tripping a full game via the JPA entity preserves board, moves and metadata")
    void jpaEntityRoundTripPreservesFullGameState() {
        PlayerId white = PlayerId.generate();
        PlayerId black = PlayerId.generate();
        Game game = Game.create(white, black, 600, 5);
        game.start();
        game.makeMove(white, Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"), null);
        game.makeMove(black, Position.fromAlgebraic("e7"), Position.fromAlgebraic("e5"), null);

        com.chess.game.infrastructure.persistence.entity.GameJpaEntity entity =
                com.chess.game.infrastructure.persistence.entity.GameJpaEntity.fromDomain(game);
        Game restored = entity.toDomain();

        assertThat(restored.getMoveCount()).isEqualTo(game.getMoveCount());
        assertThat(restored.getCurrentTurn()).isEqualTo(game.getCurrentTurn());
        assertThat(restored.moveHistory()).hasSize(game.moveHistory().size());
        // Board content matches piece-by-piece
        Map<Position, Piece> original = game.getBoard().getPieces();
        Map<Position, Piece> restoredPieces = restored.getBoard().getPieces();
        assertThat(restoredPieces).hasSameSizeAs(original);
        original.forEach((pos, piece) ->
                assertThat(restoredPieces.get(pos))
                        .as("piece at %s", pos)
                        .usingRecursiveComparison()
                        .isEqualTo(piece));
    }
}
