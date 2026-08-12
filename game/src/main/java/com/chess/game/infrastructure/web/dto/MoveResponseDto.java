package com.chess.game.infrastructure.web.dto;

import com.chess.game.domain.model.Move;

public record MoveResponseDto(
        String from,
        String to,
        String pieceType,
        String moveType,
        String capturedPieceType,
        String promotionPieceType,
        String algebraic
) {
    public static MoveResponseDto fromDomain(Move move) {
        return new MoveResponseDto(
                move.getFrom().toAlgebraic(),
                move.getTo().toAlgebraic(),
                move.getPieceType().name(),
                move.getMoveType().name(),
                move.getCapturedPieceType() != null ? move.getCapturedPieceType().name() : null,
                move.getPromotionPieceType() != null ? move.getPromotionPieceType().name() : null,
                move.toAlgebraic()
        );
    }
}
