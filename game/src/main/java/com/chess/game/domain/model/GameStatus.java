package com.chess.game.domain.model;

public enum GameStatus {
    WAITING_FOR_OPPONENT,
    IN_PROGRESS,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW_BY_AGREEMENT,
    DRAW_BY_REPETITION,
    DRAW_BY_INSUFFICIENT_MATERIAL,
    DRAW_BY_FIFTY_MOVE_RULE,
    RESIGNED,
    TIMEOUT,
    ABANDONED;

    public boolean isTerminal() {
        return this == CHECKMATE || this == STALEMATE || this == DRAW_BY_AGREEMENT
                || this == DRAW_BY_REPETITION || this == DRAW_BY_INSUFFICIENT_MATERIAL
                || this == DRAW_BY_FIFTY_MOVE_RULE || this == RESIGNED
                || this == TIMEOUT || this == ABANDONED;
    }
}
