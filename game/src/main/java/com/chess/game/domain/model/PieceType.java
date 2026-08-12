package com.chess.game.domain.model;

public enum PieceType {
    KING(0, "K"),
    QUEEN(9, "Q"),
    ROOK(5, "R"),
    BISHOP(3, "B"),
    KNIGHT(3, "N"),
    PAWN(1, "P");

    private final int value;
    private final String symbol;

    PieceType(int value, String symbol) {
        this.value = value;
        this.symbol = symbol;
    }

    public int getValue() { return value; }
    public String getSymbol() { return symbol; }
}
