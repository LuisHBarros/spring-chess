package com.chess.game.domain.model;

import java.util.Objects;

public final class Move {
    private final Position from;
    private final Position to;
    private final PieceType pieceType;
    private final MoveType moveType;
    private final PieceType capturedPieceType; // nullable
    private final PieceType promotionPieceType; // nullable

    private Move(Position from, Position to, PieceType pieceType, MoveType moveType,
                 PieceType capturedPieceType, PieceType promotionPieceType) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("From and to positions cannot be null");
        }
        if (pieceType == null || moveType == null) {
            throw new IllegalArgumentException("PieceType and MoveType cannot be null");
        }
        this.from = from;
        this.to = to;
        this.pieceType = pieceType;
        this.moveType = moveType;
        this.capturedPieceType = capturedPieceType;
        this.promotionPieceType = promotionPieceType;
    }

    public static Move of(Position from, Position to, PieceType pieceType, MoveType moveType) {
        return new Move(from, to, pieceType, moveType, null, null);
    }

    public static Move capture(Position from, Position to, PieceType pieceType, PieceType capturedPieceType) {
        return new Move(from, to, pieceType, MoveType.CAPTURE, capturedPieceType, null);
    }

    public static Move promotion(Position from, Position to, PieceType promotionPieceType, PieceType capturedPieceType) {
        return new Move(from, to, PieceType.PAWN, MoveType.PAWN_PROMOTION, capturedPieceType, promotionPieceType);
    }

    public static Move enPassant(Position from, Position to) {
        return new Move(from, to, PieceType.PAWN, MoveType.EN_PASSANT, PieceType.PAWN, null);
    }

    public static Move castlingKingside(Position from, Position to) {
        return new Move(from, to, PieceType.KING, MoveType.CASTLING_KINGSIDE, null, null);
    }

    public static Move castlingQueenside(Position from, Position to) {
        return new Move(from, to, PieceType.KING, MoveType.CASTLING_QUEENSIDE, null, null);
    }

    public static Move reconstitute(Position from, Position to, PieceType pieceType, MoveType moveType,
                                     PieceType capturedPieceType, PieceType promotionPieceType) {
        return new Move(from, to, pieceType, moveType, capturedPieceType, promotionPieceType);
    }

    public String toAlgebraic() {
        if (moveType == MoveType.CASTLING_KINGSIDE) return "O-O";
        if (moveType == MoveType.CASTLING_QUEENSIDE) return "O-O-O";
        StringBuilder sb = new StringBuilder();
        if (pieceType != PieceType.PAWN) {
            sb.append(pieceType.getSymbol());
        }
        if (capturedPieceType != null) {
            if (pieceType == PieceType.PAWN) sb.append(from.toAlgebraic().charAt(0));
            sb.append('x');
        }
        sb.append(to.toAlgebraic());
        if (promotionPieceType != null) {
            sb.append('=').append(promotionPieceType.getSymbol());
        }
        return sb.toString();
    }

    public Position getFrom() { return from; }
    public Position getTo() { return to; }
    public PieceType getPieceType() { return pieceType; }
    public MoveType getMoveType() { return moveType; }
    public PieceType getCapturedPieceType() { return capturedPieceType; }
    public PieceType getPromotionPieceType() { return promotionPieceType; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return Objects.equals(from, move.from) && Objects.equals(to, move.to)
                && pieceType == move.pieceType && moveType == move.moveType;
    }

    @Override
    public int hashCode() { return Objects.hash(from, to, pieceType, moveType); }

    @Override
    public String toString() { return toAlgebraic(); }
}
