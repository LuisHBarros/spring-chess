package com.chess.game.infrastructure.persistence.adapter;

import com.chess.game.domain.model.Game;
import com.chess.game.domain.model.GameId;
import com.chess.game.domain.model.GameStatus;
import com.chess.game.domain.model.PlayerId;
import com.chess.game.domain.repository.GameRepository;
import com.chess.game.infrastructure.persistence.entity.GameJpaEntity;
import com.chess.game.infrastructure.persistence.repository.SpringDataGameRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GameRepositoryAdapter implements GameRepository {

    private final SpringDataGameRepository repository;

    public GameRepositoryAdapter(SpringDataGameRepository repository) {
        this.repository = repository;
    }

    @Override
    public Game save(Game game) {
        GameJpaEntity entity = GameJpaEntity.fromDomain(game);
        GameJpaEntity saved = repository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Game> findById(GameId id) {
        return repository.findById(id.value()).map(GameJpaEntity::toDomain);
    }

    @Override
    public List<Game> findByPlayerId(PlayerId playerId) {
        return repository.findAllByPlayerId(playerId.value())
                .stream()
                .map(GameJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Game> findByStatus(GameStatus status) {
        return repository.findByStatus(status)
                .stream()
                .map(GameJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Game game) {
        repository.deleteById(game.getId().value());
    }
}
