package com.chess.game.infrastructure.web.dto;

import com.chess.game.domain.model.GameHistoryRecord;

import java.time.Instant;
import java.util.UUID;

public record GameHistoryResponseDto(
        UUID gameId,
        int moveNumber,
        UUID playerId,
        String from,
        String to,
        String pieceType,
        String moveType,
        String algebraic,
        String boardState,
        Instant timestamp
) {
    public static GameHistoryResponseDto fromDomain(GameHistoryRecord record) {
        return new GameHistoryResponseDto(
                record.getGameId().getValue(),
                record.getMoveNumber(),
                record.getPlayerId().getValue(),
                record.getFrom().toAlgebraic(),
                record.getTo().toAlgebraic(),
                record.getPieceType().name(),
                record.getMoveType().name(),
                record.getAlgebraic(),
                record.getBoardState(),
                record.getTimestamp()
        );
    }
}
