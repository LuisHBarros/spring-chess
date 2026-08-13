package com.chess.game.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GameHistoryRecordTest {

    @Test
    @DisplayName("Should create GameHistoryRecord with correct fields")
    void shouldCreateGameHistoryRecord() {
        GameId gameId = GameId.generate();
        PlayerId playerId = PlayerId.generate();

        GameHistoryRecord record = GameHistoryRecord.create(
                gameId,
                1,
                playerId,
                Position.fromAlgebraic("e2"),
                Position.fromAlgebraic("e4"),
                PieceType.PAWN,
                MoveType.DOUBLE_PAWN_PUSH,
                "e4",
                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR"
        );

        assertThat(record.getGameId()).isEqualTo(gameId);
        assertThat(record.getMoveNumber()).isEqualTo(1);
        assertThat(record.getPlayerId()).isEqualTo(playerId);
        assertThat(record.getFrom()).isEqualTo(Position.fromAlgebraic("e2"));
        assertThat(record.getTo()).isEqualTo(Position.fromAlgebraic("e4"));
        assertThat(record.getPieceType()).isEqualTo(PieceType.PAWN);
        assertThat(record.getMoveType()).isEqualTo(MoveType.DOUBLE_PAWN_PUSH);
        assertThat(record.getAlgebraic()).isEqualTo("e4");
        assertThat(record.getBoardState()).contains("4P3");
        assertThat(record.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("reconstitute preserves the provided timestamp")
    void reconstitutePreservesTimestamp() {
        Instant fixed = Instant.parse("2026-01-01T00:00:00Z");
        GameHistoryRecord record = GameHistoryRecord.reconstitute(
                GameId.generate(), 5, PlayerId.generate(),
                Position.fromAlgebraic("a1"), Position.fromAlgebraic("a3"),
                PieceType.ROOK, MoveType.NORMAL, "Ra3", "", fixed);

        assertThat(record.getTimestamp()).isEqualTo(fixed);
        assertThat(record.getMoveNumber()).isEqualTo(5);
    }
}
