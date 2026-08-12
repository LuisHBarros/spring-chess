package com.chess.game.domain.model;

import java.time.Instant;
import java.util.Objects;

public final class GameHistoryRecord {

    private final GameId gameId;
    private final int moveNumber;
    private final PlayerId playerId;
    private final Position from;
    private final Position to;
    private final PieceType pieceType;
    private final MoveType moveType;
    private final String algebraic;
    private final String boardState;
    private final Instant timestamp;

    private GameHistoryRecord(GameId gameId, int moveNumber, PlayerId playerId, Position from,
                              Position to, PieceType pieceType, MoveType moveType,
                              String algebraic, String boardState, Instant timestamp) {
        if (gameId == null || playerId == null || from == null || to == null || pieceType == null || moveType == null || timestamp == null) {
            throw new IllegalArgumentException("Required fields cannot be null");
        }
        if (moveNumber <= 0) {
            throw new IllegalArgumentException("Move number must be positive");
        }
        this.gameId = gameId;
        this.moveNumber = moveNumber;
        this.playerId = playerId;
        this.from = from;
        this.to = to;
        this.pieceType = pieceType;
        this.moveType = moveType;
        this.algebraic = algebraic != null ? algebraic : "";
        this.boardState = boardState != null ? boardState : "";
        this.timestamp = timestamp;
    }

    public static GameHistoryRecord create(GameId gameId, int moveNumber, PlayerId playerId, Position from,
                                            Position to, PieceType pieceType, MoveType moveType,
                                            String algebraic, String boardState) {
        return new GameHistoryRecord(gameId, moveNumber, playerId, from, to, pieceType, moveType,
                algebraic, boardState, Instant.now());
    }

    public static GameHistoryRecord reconstitute(GameId gameId, int moveNumber, PlayerId playerId, Position from,
                                                 Position to, PieceType pieceType, MoveType moveType,
                                                 String algebraic, String boardState, Instant timestamp) {
        return new GameHistoryRecord(gameId, moveNumber, playerId, from, to, pieceType, moveType,
                algebraic, boardState, timestamp);
    }

    public GameId getGameId() { return gameId; }
    public int getMoveNumber() { return moveNumber; }
    public PlayerId getPlayerId() { return playerId; }
    public Position getFrom() { return from; }
    public Position getTo() { return to; }
    public PieceType getPieceType() { return pieceType; }
    public MoveType getMoveType() { return moveType; }
    public String getAlgebraic() { return algebraic; }
    public String getBoardState() { return boardState; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameHistoryRecord that = (GameHistoryRecord) o;
        return moveNumber == that.moveNumber && Objects.equals(gameId, that.gameId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId, moveNumber);
    }

    @Override
    public String toString() {
        return String.format("GameHistoryRecord{gameId=%s, moveNumber=%d, algebraic='%s'}", gameId, moveNumber, algebraic);
    }
}
