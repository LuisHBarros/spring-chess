package com.chess.game.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Game {
    private final GameId id;
    private final PlayerId whitePlayerId;
    private final PlayerId blackPlayerId;
    private Board board;
    private GameStatus status;
    private GameResult result;
    private Color currentTurn;
    private final List<Move> moveHistory;
    private int moveCount;
    private int halfMoveClock;
    private GameClock gameClock;
    private Position enPassantTarget;
    private final Instant createdAt;
    private Instant updatedAt;

    private Game(GameId id, PlayerId whitePlayerId, PlayerId blackPlayerId, Board board, GameStatus status,
                 GameResult result, Color currentTurn, List<Move> moveHistory, int moveCount, int halfMoveClock,
                 GameClock gameClock, Position enPassantTarget, Instant createdAt, Instant updatedAt) {
        if (id == null || whitePlayerId == null || blackPlayerId == null || board == null || status == null || currentTurn == null || createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("Required fields cannot be null");
        }
        this.id = id;
        this.whitePlayerId = whitePlayerId;
        this.blackPlayerId = blackPlayerId;
        this.board = board;
        this.status = status;
        this.result = result;
        this.currentTurn = currentTurn;
        this.moveHistory = new ArrayList<>(moveHistory != null ? moveHistory : Collections.emptyList());
        this.moveCount = moveCount;
        this.halfMoveClock = halfMoveClock;
        this.gameClock = gameClock;
        this.enPassantTarget = enPassantTarget;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Game create(PlayerId white, PlayerId black, int initialTimeSec, int incrementSec) {
        Instant now = Instant.now();
        return new Game(GameId.generate(), white, black, Board.create(), GameStatus.WAITING_FOR_OPPONENT, null,
                Color.WHITE, new ArrayList<>(), 0, 0, GameClock.create(initialTimeSec, incrementSec), null, now, now);
    }

    public static Game reconstitute(GameId id, PlayerId whitePlayerId, PlayerId blackPlayerId, Board board, GameStatus status,
                                    GameResult result, Color currentTurn, List<Move> moveHistory, int moveCount, int halfMoveClock,
                                    GameClock gameClock, Position enPassantTarget, Instant createdAt, Instant updatedAt) {
        return new Game(id, whitePlayerId, blackPlayerId, board, status, result, currentTurn, moveHistory, moveCount, halfMoveClock, gameClock, enPassantTarget, createdAt, updatedAt);
    }

    public void start() {
        if (status != GameStatus.WAITING_FOR_OPPONENT) {
            throw new IllegalStateException("Game is already started or finished");
        }
        this.status = GameStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    public Move makeMove(PlayerId playerId, Position from, Position to, PieceType promotionPiece) {
        if (status.isTerminal() || status == GameStatus.WAITING_FOR_OPPONENT) {
            throw new IllegalStateException("Game is not in progress");
        }
        Color playerColor = getPlayerColor(playerId);
        if (currentTurn != playerColor) {
            throw new IllegalStateException("Not your turn");
        }

        Piece piece = board.getPieceAt(from).orElseThrow(() -> new IllegalArgumentException("No piece at source position"));
        if (piece.getColor() != currentTurn) {
            throw new IllegalArgumentException("Cannot move opponent's piece");
        }

        MoveType moveType = determineMoveType(piece, from, to);
        if (!isPseudoLegalMove(piece, to, moveType)) {
            throw new IllegalArgumentException("Illegal move for piece");
        }
        
        Optional<Piece> capturedPiece = board.getPieceAt(to);
        if (moveType == MoveType.EN_PASSANT) {
            capturedPiece = Optional.of(Piece.create(PieceType.PAWN, currentTurn.opposite(), Position.of(to.getFile(), from.getRank())));
        }

        Move move;
        if (moveType == MoveType.PAWN_PROMOTION) {
            if (promotionPiece == null) throw new IllegalArgumentException("Promotion piece required");
            move = Move.promotion(from, to, promotionPiece, capturedPiece.map(Piece::getPieceType).orElse(null));
        } else if (moveType == MoveType.CAPTURE || capturedPiece.isPresent()) {
            move = Move.capture(from, to, piece.getPieceType(), capturedPiece.get().getPieceType());
        } else if (moveType == MoveType.EN_PASSANT) {
            move = Move.enPassant(from, to);
        } else if (moveType == MoveType.CASTLING_KINGSIDE) {
            move = Move.castlingKingside(from, to);
        } else if (moveType == MoveType.CASTLING_QUEENSIDE) {
            move = Move.castlingQueenside(from, to);
        } else {
            move = Move.of(from, to, piece.getPieceType(), MoveType.NORMAL);
        }

        Board newBoard = board.movePiece(move);

        if (newBoard.isSquareAttackedBy(newBoard.getKingPosition(currentTurn), currentTurn.opposite())) {
            throw new IllegalArgumentException("Move leaves king in check");
        }

        this.board = newBoard;
        this.moveHistory.add(move);
        
        if (piece.getPieceType() == PieceType.PAWN || capturedPiece.isPresent()) {
            this.halfMoveClock = 0;
        } else {
            this.halfMoveClock++;
        }

        if (piece.getPieceType() == PieceType.PAWN && Math.abs(to.getRank() - from.getRank()) == 2) {
            this.enPassantTarget = Position.of(from.getFile(), (from.getRank() + to.getRank()) / 2);
        } else {
            this.enPassantTarget = null;
        }

        Color nextTurn = currentTurn.opposite();
        if (isCheckmate(nextTurn)) {
            this.status = GameStatus.CHECKMATE;
            this.result = currentTurn == Color.WHITE ? GameResult.WHITE_WINS : GameResult.BLACK_WINS;
        } else if (isStalemate(nextTurn)) {
            this.status = GameStatus.STALEMATE;
            this.result = GameResult.DRAW;
        } else if (isCheck(nextTurn)) {
            this.status = GameStatus.CHECK;
        } else if (this.halfMoveClock >= 100) {
            this.status = GameStatus.DRAW_BY_FIFTY_MOVE_RULE;
            this.result = GameResult.DRAW;
        } else {
            this.status = GameStatus.IN_PROGRESS;
        }

        if (currentTurn == Color.BLACK) {
            this.moveCount++;
        }
        
        this.currentTurn = nextTurn;
        this.gameClock = gameClock.addIncrement(currentTurn);
        this.updatedAt = Instant.now();

        return move;
    }

    public void resign(PlayerId playerId) {
        if (status.isTerminal()) return;
        Color playerColor = getPlayerColor(playerId);
        this.status = GameStatus.RESIGNED;
        this.result = playerColor == Color.WHITE ? GameResult.BLACK_WINS : GameResult.WHITE_WINS;
        this.updatedAt = Instant.now();
    }

    public boolean isCheck(Color color) {
        return board.isSquareAttackedBy(board.getKingPosition(color), color.opposite());
    }

    public boolean isCheckmate(Color color) {
        return isCheck(color) && !hasLegalMoves(color);
    }

    public boolean isStalemate(Color color) {
        return !isCheck(color) && !hasLegalMoves(color);
    }

    public boolean hasLegalMoves(Color color) {
        for (Piece piece : board.getAllPieces(color)) {
            for (int file = 0; file < 8; file++) {
                for (int rank = 0; rank < 8; rank++) {
                    if (isLegalMove(piece.getPosition(), Position.of(file, rank), PieceType.QUEEN)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isLegalMove(Position from, Position to, PieceType promotion) {
        try {
            Optional<Piece> p = board.getPieceAt(from);
            if (p.isEmpty()) return false;
            Piece piece = p.get();
            MoveType type = determineMoveType(piece, from, to);
            if (!isPseudoLegalMove(piece, to, type)) return false;
            
            Move move = Move.of(from, to, piece.getPieceType(), type);
            Board testBoard = board.movePiece(move);
            return !testBoard.isSquareAttackedBy(testBoard.getKingPosition(piece.getColor()), piece.getColor().opposite());
        } catch (Exception e) {
            return false;
        }
    }

    private MoveType determineMoveType(Piece piece, Position from, Position to) {
        if (piece.getPieceType() == PieceType.KING && Math.abs(to.getFile() - from.getFile()) == 2) {
            return to.getFile() == 6 ? MoveType.CASTLING_KINGSIDE : MoveType.CASTLING_QUEENSIDE;
        }
        if (piece.getPieceType() == PieceType.PAWN) {
            if (Math.abs(to.getRank() - from.getRank()) == 2) return MoveType.DOUBLE_PAWN_PUSH;
            if (from.getFile() != to.getFile() && !board.isSquareOccupied(to)) return MoveType.EN_PASSANT;
            if (to.getRank() == 0 || to.getRank() == 7) return MoveType.PAWN_PROMOTION;
        }
        return board.isSquareOccupied(to) ? MoveType.CAPTURE : MoveType.NORMAL;
    }

    public boolean isPseudoLegalMove(Piece piece, Position to, MoveType moveType) {
        Position from = piece.getPosition();
        int fileDiff = to.getFile() - from.getFile();
        int rankDiff = to.getRank() - from.getRank();
        int absFileDiff = Math.abs(fileDiff);
        int absRankDiff = Math.abs(rankDiff);

        if (absFileDiff == 0 && absRankDiff == 0) return false;
        
        Optional<Piece> targetPiece = board.getPieceAt(to);
        if (targetPiece.isPresent() && targetPiece.get().getColor() == piece.getColor()) {
            return false;
        }

        switch (piece.getPieceType()) {
            case PAWN:
                int dir = piece.getColor() == Color.WHITE ? 1 : -1;
                if (absFileDiff == 0) {
                    if (rankDiff == dir && targetPiece.isEmpty()) return true;
                    if (rankDiff == 2 * dir && !piece.hasMoved() && targetPiece.isEmpty() && !board.isSquareOccupied(Position.of(from.getFile(), from.getRank() + dir))) return true;
                } else if (absFileDiff == 1 && rankDiff == dir) {
                    return targetPiece.isPresent() || to.equals(enPassantTarget);
                }
                return false;
            case KNIGHT:
                return (absFileDiff == 2 && absRankDiff == 1) || (absFileDiff == 1 && absRankDiff == 2);
            case BISHOP:
                return absFileDiff == absRankDiff && board.isPathClear(from, to);
            case ROOK:
                return (absFileDiff == 0 || absRankDiff == 0) && board.isPathClear(from, to);
            case QUEEN:
                return (absFileDiff == absRankDiff || absFileDiff == 0 || absRankDiff == 0) && board.isPathClear(from, to);
            case KING:
                if (absFileDiff <= 1 && absRankDiff <= 1) return true;
                if (!piece.hasMoved() && absRankDiff == 0 && absFileDiff == 2) {
                    if (isCheck(piece.getColor())) return false;
                    int rookFile = fileDiff > 0 ? 7 : 0;
                    Optional<Piece> rook = board.getPieceAt(Position.of(rookFile, from.getRank()));
                    if (rook.isPresent() && rook.get().getPieceType() == PieceType.ROOK && !rook.get().hasMoved()) {
                        if (!board.isPathClear(from, Position.of(rookFile, from.getRank()))) return false;
                        Position intermediate = Position.of(fileDiff > 0 ? 5 : 3, from.getRank());
                        return !board.isSquareAttackedBy(intermediate, piece.getColor().opposite());
                    }
                }
                return false;
            default:
                return false;
        }
    }

    public Color getPlayerColor(PlayerId playerId) {
        if (playerId.equals(whitePlayerId)) return Color.WHITE;
        if (playerId.equals(blackPlayerId)) return Color.BLACK;
        throw new IllegalArgumentException("Player not in this game");
    }

    public List<Move> moveHistory() { return Collections.unmodifiableList(moveHistory); }
    public GameId getId() { return id; }
    public PlayerId getWhitePlayerId() { return whitePlayerId; }
    public PlayerId getBlackPlayerId() { return blackPlayerId; }
    public Board getBoard() { return board; }
    public GameStatus getStatus() { return status; }
    public GameResult getResult() { return result; }
    public Color getCurrentTurn() { return currentTurn; }
    public int getMoveCount() { return moveCount; }
    public int getHalfMoveClock() { return halfMoveClock; }
    public GameClock getGameClock() { return gameClock; }
    public Position getEnPassantTarget() { return enPassantTarget; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
