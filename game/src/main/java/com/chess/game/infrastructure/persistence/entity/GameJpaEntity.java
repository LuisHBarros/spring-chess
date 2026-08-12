package com.chess.game.infrastructure.persistence.entity;

import com.chess.game.domain.model.Board;
import com.chess.game.domain.model.Color;
import com.chess.game.domain.model.Game;
import com.chess.game.domain.model.GameClock;
import com.chess.game.domain.model.GameId;
import com.chess.game.domain.model.GameResult;
import com.chess.game.domain.model.GameStatus;
import com.chess.game.domain.model.PlayerId;
import com.chess.game.domain.model.Position;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Entity
@Table(name = "games")
public class GameJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "white_player_id")
    private UUID whitePlayerId;

    @Column(name = "black_player_id")
    private UUID blackPlayerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GameStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    private GameResult result;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_turn")
    private Color currentTurn;

    @Column(name = "move_count")
    private int moveCount;

    @Column(name = "half_move_clock")
    private int halfMoveClock;

    @Column(name = "initial_time_seconds")
    private int initialTimeSeconds;

    @Column(name = "increment_seconds")
    private int incrementSeconds;

    @Column(name = "white_time_remaining_ms")
    private long whiteTimeRemainingMs;

    @Column(name = "black_time_remaining_ms")
    private long blackTimeRemainingMs;

    @Column(name = "board_json", columnDefinition = "TEXT")
    private String boardJson;

    @Column(name = "moves_json", columnDefinition = "TEXT")
    private String movesJson;

    @Column(name = "en_passant_target")
    private String enPassantTarget;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public GameJpaEntity() {}

    public GameJpaEntity(UUID id, UUID whitePlayerId, UUID blackPlayerId, GameStatus status, GameResult result,
                         Color currentTurn, int moveCount, int halfMoveClock, int initialTimeSeconds,
                         int incrementSeconds, long whiteTimeRemainingMs, long blackTimeRemainingMs,
                         String boardJson, String movesJson, String enPassantTarget, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.whitePlayerId = whitePlayerId;
        this.blackPlayerId = blackPlayerId;
        this.status = status;
        this.result = result;
        this.currentTurn = currentTurn;
        this.moveCount = moveCount;
        this.halfMoveClock = halfMoveClock;
        this.initialTimeSeconds = initialTimeSeconds;
        this.incrementSeconds = incrementSeconds;
        this.whiteTimeRemainingMs = whiteTimeRemainingMs;
        this.blackTimeRemainingMs = blackTimeRemainingMs;
        this.boardJson = boardJson;
        this.movesJson = movesJson;
        this.enPassantTarget = enPassantTarget;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static GameJpaEntity fromDomain(Game game) {
        return new GameJpaEntity(
                game.getId().getValue(),
                game.getWhitePlayerId().getValue(),
                game.getBlackPlayerId().getValue(),
                game.getStatus(),
                game.getResult(),
                game.getCurrentTurn(),
                game.getMoveCount(),
                game.getHalfMoveClock(),
                game.getGameClock().getInitialTimeSeconds(),
                game.getGameClock().getIncrementSeconds(),
                game.getGameClock().getWhiteTimeRemainingMs(),
                game.getGameClock().getBlackTimeRemainingMs(),
                "{}",
                "[]",
                game.getEnPassantTarget() != null ? game.getEnPassantTarget().toAlgebraic() : null,
                game.getCreatedAt(),
                game.getUpdatedAt()
        );
    }

    public Game toDomain() {
        GameClock clock = GameClock.reconstitute(
                initialTimeSeconds,
                incrementSeconds,
                whiteTimeRemainingMs,
                blackTimeRemainingMs
        );
        Position epTarget = enPassantTarget != null ? Position.fromAlgebraic(enPassantTarget) : null;

        return Game.reconstitute(
                GameId.from(id),
                PlayerId.from(whitePlayerId),
                PlayerId.from(blackPlayerId),
                Board.create(),
                status,
                result,
                currentTurn,
                new ArrayList<>(),
                moveCount,
                halfMoveClock,
                clock,
                epTarget,
                createdAt,
                updatedAt
        );
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getWhitePlayerId() { return whitePlayerId; }
    public void setWhitePlayerId(UUID whitePlayerId) { this.whitePlayerId = whitePlayerId; }
    public UUID getBlackPlayerId() { return blackPlayerId; }
    public void setBlackPlayerId(UUID blackPlayerId) { this.blackPlayerId = blackPlayerId; }
    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }
    public GameResult getResult() { return result; }
    public void setResult(GameResult result) { this.result = result; }
    public Color getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(Color currentTurn) { this.currentTurn = currentTurn; }
    public int getMoveCount() { return moveCount; }
    public void setMoveCount(int moveCount) { this.moveCount = moveCount; }
    public int getHalfMoveClock() { return halfMoveClock; }
    public void setHalfMoveClock(int halfMoveClock) { this.halfMoveClock = halfMoveClock; }
    public int getInitialTimeSeconds() { return initialTimeSeconds; }
    public void setInitialTimeSeconds(int initialTimeSeconds) { this.initialTimeSeconds = initialTimeSeconds; }
    public int getIncrementSeconds() { return incrementSeconds; }
    public void setIncrementSeconds(int incrementSeconds) { this.incrementSeconds = incrementSeconds; }
    public long getWhiteTimeRemainingMs() { return whiteTimeRemainingMs; }
    public void setWhiteTimeRemainingMs(long whiteTimeRemainingMs) { this.whiteTimeRemainingMs = whiteTimeRemainingMs; }
    public long getBlackTimeRemainingMs() { return blackTimeRemainingMs; }
    public void setBlackTimeRemainingMs(long blackTimeRemainingMs) { this.blackTimeRemainingMs = blackTimeRemainingMs; }
    public String getBoardJson() { return boardJson; }
    public void setBoardJson(String boardJson) { this.boardJson = boardJson; }
    public String getMovesJson() { return movesJson; }
    public void setMovesJson(String movesJson) { this.movesJson = movesJson; }
    public String getEnPassantTarget() { return enPassantTarget; }
    public void setEnPassantTarget(String enPassantTarget) { this.enPassantTarget = enPassantTarget; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
