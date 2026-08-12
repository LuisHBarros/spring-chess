package com.chess.game.infrastructure.web.dto;

import com.chess.game.domain.model.Game;
import com.chess.game.domain.model.Piece;
import com.chess.game.domain.model.Position;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record GameResponseDto(
        UUID id,
        UUID whitePlayerId,
        UUID blackPlayerId,
        String status,
        String result,
        String currentTurn,
        int moveCount,
        List<String> moves,
        Map<String, String> boardPieces,
        long whiteTimeRemainingMs,
        long blackTimeRemainingMs,
        Instant createdAt,
        Instant updatedAt
) {
    public static GameResponseDto fromDomain(Game game) {
        List<String> moveNotations = game.moveHistory().stream()
                .map(m -> m.toAlgebraic())
                .collect(Collectors.toList());

        Map<String, String> pieces = game.getBoard().getPieces().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toAlgebraic(),
                        e -> {
                            Piece p = e.getValue();
                            return p.getColor().name().charAt(0) + p.getPieceType().getSymbol();
                        }
                ));

        return new GameResponseDto(
                game.getId().getValue(),
                game.getWhitePlayerId().getValue(),
                game.getBlackPlayerId().getValue(),
                game.getStatus().name(),
                game.getResult() != null ? game.getResult().name() : null,
                game.getCurrentTurn().name(),
                game.getMoveCount(),
                moveNotations,
                pieces,
                game.getGameClock().getWhiteTimeRemainingMs(),
                game.getGameClock().getBlackTimeRemainingMs(),
                game.getCreatedAt(),
                game.getUpdatedAt()
        );
    }
}
