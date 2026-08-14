package com.chess.game.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    private PlayerId whitePlayer;
    private PlayerId blackPlayer;
    private Game game;

    @BeforeEach
    void setUp() {
        whitePlayer = PlayerId.from(UUID.randomUUID());
        blackPlayer = PlayerId.from(UUID.randomUUID());
        game = Game.create(whitePlayer, blackPlayer, 600, 5);
        game.start();
    }

    @Test
    @DisplayName("Should initialize game correctly and set WHITE as current turn")
    void shouldInitializeGame() {
        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(game.getCurrentTurn()).isEqualTo(Color.WHITE);
        assertThat(game.getWhitePlayerId()).isEqualTo(whitePlayer);
        assertThat(game.getBlackPlayerId()).isEqualTo(blackPlayer);
        assertThat(game.getMoveCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should execute legal pawn move e2 to e4")
    void shouldMakeLegalMove() {
        Move move = game.makeMove(whitePlayer, Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"), null);

        assertThat(move.getMoveType()).isEqualTo(MoveType.DOUBLE_PAWN_PUSH);
        assertThat(game.getCurrentTurn()).isEqualTo(Color.BLACK);
        assertThat(game.getBoard().getPieceAt(Position.fromAlgebraic("e4"))).isPresent();
        assertThat(game.getBoard().getPieceAt(Position.fromAlgebraic("e2"))).isEmpty();
        assertThat(game.getMoveCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should reject move if not player's turn")
    void shouldRejectMoveIfNotPlayersTurn() {
        assertThatThrownBy(() -> game.makeMove(blackPlayer, Position.fromAlgebraic("e7"), Position.fromAlgebraic("e5"), null))
                .isInstanceOf(com.chess.game.domain.exception.NotPlayerTurnException.class)
                .hasMessageContaining("Not your turn");
    }

    @Test
    @DisplayName("Should allow valid Kingside castling")
    void shouldAllowValidKingsideCastling() {
        // Move pawn e2-e4
        game.makeMove(whitePlayer, Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"), null);
        // Black e7-e5
        game.makeMove(blackPlayer, Position.fromAlgebraic("e7"), Position.fromAlgebraic("e5"), null);
        // White knight g1-f3
        game.makeMove(whitePlayer, Position.fromAlgebraic("g1"), Position.fromAlgebraic("f3"), null);
        // Black knight b8-c6
        game.makeMove(blackPlayer, Position.fromAlgebraic("b8"), Position.fromAlgebraic("c6"), null);
        // White bishop f1-c4
        game.makeMove(whitePlayer, Position.fromAlgebraic("f1"), Position.fromAlgebraic("c4"), null);
        // Black bishop f8-c5
        game.makeMove(blackPlayer, Position.fromAlgebraic("f8"), Position.fromAlgebraic("c5"), null);

        // Now White can castle Kingside e1-g1
        Move castleMove = game.makeMove(whitePlayer, Position.fromAlgebraic("e1"), Position.fromAlgebraic("g1"), null);

        assertThat(castleMove.getMoveType()).isEqualTo(MoveType.CASTLING_KINGSIDE);
        assertThat(game.getBoard().getPieceAt(Position.fromAlgebraic("g1")).get().getPieceType()).isEqualTo(PieceType.KING);
        assertThat(game.getBoard().getPieceAt(Position.fromAlgebraic("f1")).get().getPieceType()).isEqualTo(PieceType.ROOK);
    }

    @Test
    @DisplayName("Should PREVENT castling when intermediate square (f1) is attacked")
    void shouldPreventCastlingWhenIntermediateSquareIsAttacked() {
        // Set up custom board where White king is on e1, rook on h1, and Black bishop controls f1
        Map<Position, Piece> pieces = new HashMap<>();
        Position e1 = Position.fromAlgebraic("e1");
        Position h1 = Position.fromAlgebraic("h1");
        Position a6 = Position.fromAlgebraic("a6"); // Black bishop on a6 attacks f1 via diagonal a6-b5-c4-d3-e2-f1
        Position e8 = Position.fromAlgebraic("e8");

        pieces.put(e1, Piece.create(PieceType.KING, Color.WHITE, e1));
        pieces.put(h1, Piece.create(PieceType.ROOK, Color.WHITE, h1));
        pieces.put(a6, Piece.create(PieceType.BISHOP, Color.BLACK, a6));
        pieces.put(e8, Piece.create(PieceType.KING, Color.BLACK, e8));

        Board customBoard = Board.reconstitute(pieces);
        Instant now = Instant.now();
        Game customGame = Game.reconstitute(
                GameId.generate(),
                whitePlayer,
                blackPlayer,
                customBoard,
                GameStatus.IN_PROGRESS,
                null,
                Color.WHITE,
                Collections.emptyList(),
                0,
                0,
                GameClock.create(600, 5),
                null,
                now,
                now
        );

        // Verify f1 is attacked by Black bishop
        assertThat(customBoard.isSquareAttackedBy(Position.fromAlgebraic("f1"), Color.BLACK)).isTrue();

        // Attempt castling e1 to g1 should fail as illegal move
        assertThatThrownBy(() -> customGame.makeMove(whitePlayer, e1, Position.fromAlgebraic("g1"), null))
                .isInstanceOf(com.chess.game.domain.exception.InvalidMoveException.class)
                .hasMessageContaining("Illegal move");
    }

    @Test
    @DisplayName("Should PREVENT castling when king is in check")
    void shouldPreventCastlingWhenKingIsInCheck() {
        Map<Position, Piece> pieces = new HashMap<>();
        Position e1 = Position.fromAlgebraic("e1");
        Position h1 = Position.fromAlgebraic("h1");
        Position e5 = Position.fromAlgebraic("e5"); // Black rook checking White King on e1
        Position a8 = Position.fromAlgebraic("a8");

        pieces.put(e1, Piece.create(PieceType.KING, Color.WHITE, e1));
        pieces.put(h1, Piece.create(PieceType.ROOK, Color.WHITE, h1));
        pieces.put(e5, Piece.create(PieceType.ROOK, Color.BLACK, e5));
        pieces.put(a8, Piece.create(PieceType.KING, Color.BLACK, a8));

        Board customBoard = Board.reconstitute(pieces);
        Instant now = Instant.now();
        Game customGame = Game.reconstitute(
                GameId.generate(),
                whitePlayer,
                blackPlayer,
                customBoard,
                GameStatus.IN_PROGRESS,
                null,
                Color.WHITE,
                Collections.emptyList(),
                0,
                0,
                GameClock.create(600, 5),
                null,
                now,
                now
        );

        assertThat(customGame.isCheck(Color.WHITE)).isTrue();

        assertThatThrownBy(() -> customGame.makeMove(whitePlayer, e1, Position.fromAlgebraic("g1"), null))
                .isInstanceOf(com.chess.game.domain.exception.InvalidMoveException.class)
                .hasMessageContaining("Illegal move");
    }

    @Test
    @DisplayName("Should PREVENT castling when destination square (g1) is attacked")
    void shouldPreventCastlingWhenDestinationSquareIsAttacked() {
        // White king on e1, rook on h1; Black bishop on h2 attacks the castling destination g1
        Map<Position, Piece> pieces = new HashMap<>();
        Position e1 = Position.fromAlgebraic("e1");
        Position h1 = Position.fromAlgebraic("h1");
        Position h2 = Position.fromAlgebraic("h2"); // Black bishop attacks g1
        Position e8 = Position.fromAlgebraic("e8");

        pieces.put(e1, Piece.create(PieceType.KING, Color.WHITE, e1));
        pieces.put(h1, Piece.create(PieceType.ROOK, Color.WHITE, h1));
        pieces.put(h2, Piece.create(PieceType.BISHOP, Color.BLACK, h2));
        pieces.put(e8, Piece.create(PieceType.KING, Color.BLACK, e8));

        Board customBoard = Board.reconstitute(pieces);
        Instant now = Instant.now();
        Game customGame = Game.reconstitute(
                GameId.generate(),
                whitePlayer,
                blackPlayer,
                customBoard,
                GameStatus.IN_PROGRESS,
                null,
                Color.WHITE,
                Collections.emptyList(),
                0,
                0,
                GameClock.create(600, 5),
                null,
                now,
                now
        );

        // g1 is attacked, but f1 is not
        assertThat(customBoard.isSquareAttackedBy(Position.fromAlgebraic("g1"), Color.BLACK)).isTrue();
        assertThat(customBoard.isSquareAttackedBy(Position.fromAlgebraic("f1"), Color.BLACK)).isFalse();

        assertThatThrownBy(() -> customGame.makeMove(whitePlayer, e1, Position.fromAlgebraic("g1"), null))
                .isInstanceOf(com.chess.game.domain.exception.InvalidMoveException.class)
                .hasMessageContaining("Illegal move");
    }

    @Test
    @DisplayName("Should execute Scholar's Mate and finish game in CHECKMATE")
    void shouldExecuteScholarsMate() {
        // 1. e4 e5
        game.makeMove(whitePlayer, Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"), null);
        game.makeMove(blackPlayer, Position.fromAlgebraic("e7"), Position.fromAlgebraic("e5"), null);

        // 2. Qh5 Nc6
        game.makeMove(whitePlayer, Position.fromAlgebraic("d1"), Position.fromAlgebraic("h5"), null);
        game.makeMove(blackPlayer, Position.fromAlgebraic("b8"), Position.fromAlgebraic("c6"), null);

        // 3. Bc4 Nf6
        game.makeMove(whitePlayer, Position.fromAlgebraic("f1"), Position.fromAlgebraic("c4"), null);
        game.makeMove(blackPlayer, Position.fromAlgebraic("g8"), Position.fromAlgebraic("f6"), null);

        // 4. Qxf7# Checkmate
        game.makeMove(whitePlayer, Position.fromAlgebraic("h5"), Position.fromAlgebraic("f7"), null);

        assertThat(game.getStatus()).isEqualTo(GameStatus.CHECKMATE);
        assertThat(game.getStatus().isTerminal()).isTrue();
        assertThat(game.getResult()).isEqualTo(GameResult.WHITE_WINS);
    }

    @Test
    @DisplayName("Should handle resignation correctly")
    void shouldHandleResignation() {
        game.resign(whitePlayer);

        assertThat(game.getStatus()).isEqualTo(GameStatus.RESIGNED);
        assertThat(game.getStatus().isTerminal()).isTrue();
        assertThat(game.getResult()).isEqualTo(GameResult.BLACK_WINS);
    }
}
