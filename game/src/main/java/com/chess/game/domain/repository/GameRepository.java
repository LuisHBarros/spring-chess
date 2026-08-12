package com.chess.game.domain.repository;

import com.chess.game.domain.model.Game;
import com.chess.game.domain.model.GameId;
import com.chess.game.domain.model.GameStatus;
import com.chess.game.domain.model.PlayerId;

import java.util.List;
import java.util.Optional;

public interface GameRepository {
    Game save(Game game);
    Optional<Game> findById(GameId id);
    List<Game> findByPlayerId(PlayerId playerId);
    List<Game> findByStatus(GameStatus status);
    void delete(Game game);
}
