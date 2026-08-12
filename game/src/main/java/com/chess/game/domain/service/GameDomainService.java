package com.chess.game.domain.service;

import com.chess.game.domain.exception.GameNotFoundException;
import com.chess.game.domain.model.*;
import com.chess.game.domain.port.GameEventPublisherPort;
import com.chess.game.domain.repository.GameHistoryRepository;
import com.chess.game.domain.repository.GameRepository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameDomainService {
    private final GameRepository gameRepository;
    private final GameEventPublisherPort eventPublisher;
    private final GameHistoryRepository historyRepository;

    public GameDomainService(GameRepository gameRepository, GameEventPublisherPort eventPublisher) {
        this(gameRepository, eventPublisher, null);
    }

    public GameDomainService(GameRepository gameRepository, GameEventPublisherPort eventPublisher,
                             GameHistoryRepository historyRepository) {
        if (gameRepository == null) throw new IllegalArgumentException("GameRepository cannot be null");
        if (eventPublisher == null) throw new IllegalArgumentException("GameEventPublisherPort cannot be null");
        this.gameRepository = gameRepository;
        this.eventPublisher = eventPublisher;
        this.historyRepository = historyRepository;
    }

    public Game createGame(PlayerId whitePlayerId, PlayerId blackPlayerId,
                           int initialTimeSeconds, int incrementSeconds) {
        Game game = Game.create(whitePlayerId, blackPlayerId, initialTimeSeconds, incrementSeconds);
        Game saved = gameRepository.save(game);
        publishEvent("GAME_CREATED", saved);
        return saved;
    }

    public Game startGame(GameId gameId) {
        Game game = findGame(gameId);
        game.start();
        Game saved = gameRepository.save(game);
        publishEvent("GAME_STARTED", saved);
        return saved;
    }

    public Move makeMove(GameId gameId, PlayerId playerId, Position from, Position to, PieceType promotionPiece) {
        Game game = findGame(gameId);
        Move move = game.makeMove(playerId, from, to, promotionPiece);
        Game saved = gameRepository.save(game);

        if (historyRepository != null) {
            GameHistoryRecord record = GameHistoryRecord.create(
                    gameId,
                    saved.moveHistory().size(),
                    playerId,
                    from,
                    to,
                    move.getPieceType(),
                    move.getMoveType(),
                    move.toAlgebraic(),
                    ""
            );
            historyRepository.saveMoveRecord(record);
        }

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("move", move.toAlgebraic());
        eventData.put("status", saved.getStatus().name());
        eventData.put("currentTurn", saved.getCurrentTurn().name());
        if (saved.getResult() != null) {
            eventData.put("result", saved.getResult().name());
        }
        eventPublisher.publishGameEvent("MOVE_MADE", saved.getId().toString(), eventData);
        return move;
    }

    public Game resignGame(GameId gameId, PlayerId playerId) {
        Game game = findGame(gameId);
        game.resign(playerId);
        Game saved = gameRepository.save(game);
        publishEvent("GAME_ENDED", saved);
        return saved;
    }

    public Game getGame(GameId gameId) {
        return findGame(gameId);
    }

    public List<Game> getPlayerGames(PlayerId playerId) {
        return gameRepository.findByPlayerId(playerId);
    }

    public List<GameHistoryRecord> getGameHistory(GameId gameId) {
        if (historyRepository != null) {
            return historyRepository.findByGameId(gameId);
        }
        return Collections.emptyList();
    }

    private Game findGame(GameId gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));
    }

    private void publishEvent(String eventType, Game game) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("whitePlayerId", game.getWhitePlayerId().toString());
        eventData.put("blackPlayerId", game.getBlackPlayerId().toString());
        eventData.put("status", game.getStatus().name());
        if (game.getResult() != null) {
            eventData.put("result", game.getResult().name());
        }
        eventPublisher.publishGameEvent(eventType, game.getId().toString(), eventData);
    }
}
