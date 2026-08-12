package com.chess.game.domain.model;

import java.util.Objects;

public class Piece {
    private final PieceType pieceType;
    private final Color color;
    private final Position position;
    private final boolean hasMoved;

    private Piece(PieceType pieceType, Color color, Position position, boolean hasMoved) {
        if (pieceType == null) throw new IllegalArgumentException("PieceType cannot be null");
        if (color == null) throw new IllegalArgumentException("Color cannot be null");
        if (position == null) throw new IllegalArgumentException("Position cannot be null");
        this.pieceType = pieceType;
        this.color = color;
        this.position = position;
        this.hasMoved = hasMoved;
    }

    public static Piece create(PieceType pieceType, Color color, Position position) {
        return new Piece(pieceType, color, position, false);
    }

    public static Piece reconstitute(PieceType pieceType, Color color, Position position, boolean hasMoved) {
        return new Piece(pieceType, color, position, hasMoved);
    }

    public Piece moveTo(Position newPosition) {
        return new Piece(this.pieceType, this.color, newPosition, true);
    }

    public PieceType getPieceType() { return pieceType; }
    public Color getColor() { return color; }
    public Position getPosition() { return position; }
    public boolean hasMoved() { return hasMoved; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return pieceType == piece.pieceType && color == piece.color && Objects.equals(position, piece.position);
    }

    @Override
    public int hashCode() { return Objects.hash(pieceType, color, position); }

    @Override
    public String toString() {
        return color.name().charAt(0) + pieceType.getSymbol() + position.toAlgebraic();
    }
}
