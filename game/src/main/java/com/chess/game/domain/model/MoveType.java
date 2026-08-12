package com.chess.game.domain.model;

public enum MoveType {
    NORMAL,
    CAPTURE,
    CASTLING_KINGSIDE,
    CASTLING_QUEENSIDE,
    EN_PASSANT,
    PAWN_PROMOTION,
    DOUBLE_PAWN_PUSH
}
