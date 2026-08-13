package com.chess.game.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GameHistoryRecordTest {

    @Test
    @DisplayName("Should create GameHistoryRecord with correct fields")
    void shouldCreateGameHistoryRecord() {
        UUID gameId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        Instant now = Instant.now();

        GameHistoryRecord record = new GameHistoryRecord(
                gameId,
                1,
                playerId,
                "e2",
                "e4",
                "DOUBLE_PAWN_PUSH",
                null,
                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR",
                now
        );

        assertThat(record.gameId()).isEqualTo(gameId);
        assertThat(record.moveNumber()).isEqualTo(1);
        assertThat(record.playerId()).isEqualTo(playerId);
        assertThat(record.fromSquare()).isEqualTo("e2");
        assertThat(record.toSquare()).isEqualTo("e4");
        assertThat(record.moveType()).isEqualTo("DOUBLE_PAWN_PUSH");
        assertThat(record.fenAfter()).contains("4P3");
        assertThat(record.timestamp()).isEqualTo(now);
    }
}
