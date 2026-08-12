package com.chess.game.domain.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Board {
    private final Map<Position, Piece> pieces;

    private Board(Map<Position, Piece> pieces) {
        this.pieces = new HashMap<>(pieces);
    }

    public static Board create() {
        Map<Position, Piece> initialPieces = new HashMap<>();
        
        // Pawns
        for (int file = 0; file < 8; file++) {
            initialPieces.put(Position.of(file, 1), Piece.create(PieceType.PAWN, Color.WHITE, Position.of(file, 1)));
            initialPieces.put(Position.of(file, 6), Piece.create(PieceType.PAWN, Color.BLACK, Position.of(file, 6)));
        }
        
        // Rooks
        initialPieces.put(Position.of(0, 0), Piece.create(PieceType.ROOK, Color.WHITE, Position.of(0, 0)));
        initialPieces.put(Position.of(7, 0), Piece.create(PieceType.ROOK, Color.WHITE, Position.of(7, 0)));
        initialPieces.put(Position.of(0, 7), Piece.create(PieceType.ROOK, Color.BLACK, Position.of(0, 7)));
        initialPieces.put(Position.of(7, 7), Piece.create(PieceType.ROOK, Color.BLACK, Position.of(7, 7)));
        
        // Knights
        initialPieces.put(Position.of(1, 0), Piece.create(PieceType.KNIGHT, Color.WHITE, Position.of(1, 0)));
        initialPieces.put(Position.of(6, 0), Piece.create(PieceType.KNIGHT, Color.WHITE, Position.of(6, 0)));
        initialPieces.put(Position.of(1, 7), Piece.create(PieceType.KNIGHT, Color.BLACK, Position.of(1, 7)));
        initialPieces.put(Position.of(6, 7), Piece.create(PieceType.KNIGHT, Color.BLACK, Position.of(6, 7)));
        
        // Bishops
        initialPieces.put(Position.of(2, 0), Piece.create(PieceType.BISHOP, Color.WHITE, Position.of(2, 0)));
        initialPieces.put(Position.of(5, 0), Piece.create(PieceType.BISHOP, Color.WHITE, Position.of(5, 0)));
        initialPieces.put(Position.of(2, 7), Piece.create(PieceType.BISHOP, Color.BLACK, Position.of(2, 7)));
        initialPieces.put(Position.of(5, 7), Piece.create(PieceType.BISHOP, Color.BLACK, Position.of(5, 7)));
        
        // Queens
        initialPieces.put(Position.of(3, 0), Piece.create(PieceType.QUEEN, Color.WHITE, Position.of(3, 0)));
        initialPieces.put(Position.of(3, 7), Piece.create(PieceType.QUEEN, Color.BLACK, Position.of(3, 7)));
        
        // Kings
        initialPieces.put(Position.of(4, 0), Piece.create(PieceType.KING, Color.WHITE, Position.of(4, 0)));
        initialPieces.put(Position.of(4, 7), Piece.create(PieceType.KING, Color.BLACK, Position.of(4, 7)));
        
        return new Board(initialPieces);
    }

    public static Board reconstitute(Map<Position, Piece> pieces) {
        return new Board(pieces);
    }

    public Optional<Piece> getPieceAt(Position position) {
        return Optional.ofNullable(pieces.get(position));
    }

    public Board movePiece(Move move) {
        Map<Position, Piece> newPieces = new HashMap<>(this.pieces);
        Piece movingPiece = newPieces.remove(move.getFrom());
        if (movingPiece == null) {
            throw new IllegalArgumentException("No piece at source position");
        }

        Piece newPiece = movingPiece.moveTo(move.getTo());
        
        if (move.getMoveType() == MoveType.PAWN_PROMOTION) {
            newPiece = Piece.reconstitute(move.getPromotionPieceType(), newPiece.getColor(), move.getTo(), true);
        }

        newPieces.put(move.getTo(), newPiece);

        if (move.getMoveType() == MoveType.EN_PASSANT) {
            int captureRank = movingPiece.getColor() == Color.WHITE ? 4 : 3;
            newPieces.remove(Position.of(move.getTo().getFile(), captureRank));
        } else if (move.getMoveType() == MoveType.CASTLING_KINGSIDE) {
            Position rookFrom = Position.of(7, movingPiece.getColor() == Color.WHITE ? 0 : 7);
            Position rookTo = Position.of(5, movingPiece.getColor() == Color.WHITE ? 0 : 7);
            Piece rook = newPieces.remove(rookFrom);
            if (rook != null) {
                newPieces.put(rookTo, rook.moveTo(rookTo));
            }
        } else if (move.getMoveType() == MoveType.CASTLING_QUEENSIDE) {
            Position rookFrom = Position.of(0, movingPiece.getColor() == Color.WHITE ? 0 : 7);
            Position rookTo = Position.of(3, movingPiece.getColor() == Color.WHITE ? 0 : 7);
            Piece rook = newPieces.remove(rookFrom);
            if (rook != null) {
                newPieces.put(rookTo, rook.moveTo(rookTo));
            }
        }

        return new Board(newPieces);
    }

    public boolean isSquareOccupied(Position position) {
        return pieces.containsKey(position);
    }

    public boolean isSquareAttackedBy(Position position, Color color) {
        for (Piece piece : pieces.values()) {
            if (piece.getColor() == color) {
                if (attacks(piece, position)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean attacks(Piece piece, Position target) {
        Position source = piece.getPosition();
        int fileDiff = target.getFile() - source.getFile();
        int rankDiff = target.getRank() - source.getRank();
        int absFileDiff = Math.abs(fileDiff);
        int absRankDiff = Math.abs(rankDiff);

        if (absFileDiff == 0 && absRankDiff == 0) return false;

        switch (piece.getPieceType()) {
            case PAWN:
                int direction = piece.getColor() == Color.WHITE ? 1 : -1;
                return absFileDiff == 1 && rankDiff == direction;
            case KNIGHT:
                return (absFileDiff == 2 && absRankDiff == 1) || (absFileDiff == 1 && absRankDiff == 2);
            case BISHOP:
                return absFileDiff == absRankDiff && isPathClear(source, target);
            case ROOK:
                return (absFileDiff == 0 || absRankDiff == 0) && isPathClear(source, target);
            case QUEEN:
                return (absFileDiff == absRankDiff || absFileDiff == 0 || absRankDiff == 0) && isPathClear(source, target);
            case KING:
                return absFileDiff <= 1 && absRankDiff <= 1;
            default:
                return false;
        }
    }

    public Position getKingPosition(Color color) {
        return pieces.values().stream()
                .filter(p -> p.getColor() == color && p.getPieceType() == PieceType.KING)
                .map(Piece::getPosition)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("King not found"));
    }

    public boolean isPathClear(Position from, Position to) {
        int fileDiff = to.getFile() - from.getFile();
        int rankDiff = to.getRank() - from.getRank();
        
        int fileStep = Integer.compare(fileDiff, 0);
        int rankStep = Integer.compare(rankDiff, 0);

        int currentFile = from.getFile() + fileStep;
        int currentRank = from.getRank() + rankStep;

        while (currentFile != to.getFile() || currentRank != to.getRank()) {
            if (isSquareOccupied(Position.of(currentFile, currentRank))) {
                return false;
            }
            currentFile += fileStep;
            currentRank += rankStep;
        }
        return true;
    }

    public List<Piece> getAllPieces() {
        return new ArrayList<>(pieces.values());
    }

    public List<Piece> getAllPieces(Color color) {
        return pieces.values().stream()
                .filter(p -> p.getColor() == color)
                .collect(Collectors.toList());
    }

    public Map<Position, Piece> getPieces() {
        return Collections.unmodifiableMap(pieces);
    }
}
