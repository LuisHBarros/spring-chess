package com.chess.game.domain.repository;

import com.chess.game.domain.model.GameHistoryRecord;
import com.chess.game.domain.model.GameId;

import java.util.List;

public interface GameHistoryRepository {
    void saveMoveRecord(GameHistoryRecord record);
    List<GameHistoryRecord> findByGameId(GameId gameId);
}
