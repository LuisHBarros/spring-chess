package com.chess.social.infrastructure.persistence.adapter;

import com.chess.social.domain.model.Friendship;
import com.chess.social.domain.model.FriendshipId;
import com.chess.social.domain.model.FriendshipStatus;
import com.chess.social.domain.model.UserId;
import com.chess.social.domain.repository.FriendshipRepository;
import com.chess.social.infrastructure.persistence.entity.FriendshipJpaEntity;
import com.chess.social.infrastructure.persistence.repository.SpringDataFriendshipRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FriendshipRepositoryAdapter implements FriendshipRepository {

    private final SpringDataFriendshipRepository repository;

    public FriendshipRepositoryAdapter(SpringDataFriendshipRepository repository) {
        this.repository = repository;
    }

    @Override
    public Friendship save(Friendship friendship) {
        FriendshipJpaEntity entity = FriendshipJpaEntity.fromDomain(friendship);
        FriendshipJpaEntity saved = repository.saveAndFlush(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Friendship> findById(FriendshipId id) {
        return repository.findById(id.getValue()).map(FriendshipJpaEntity::toDomain);
    }

    @Override
    public Optional<Friendship> findBetweenUsers(UserId requesterId, UserId addresseeId) {
        return repository.findBetweenUsers(requesterId.getValue(), addresseeId.getValue())
                .map(FriendshipJpaEntity::toDomain);
    }

    @Override
    public List<Friendship> findAllByUserIdAndStatus(UserId userId, FriendshipStatus status) {
        return repository.findAllByUserIdAndStatus(userId.getValue(), status).stream()
                .map(FriendshipJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Friendship> findAllByUserId(UserId userId) {
        return repository.findAllByUserId(userId.getValue()).stream()
                .map(FriendshipJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Friendship friendship) {
        repository.deleteById(friendship.getId().getValue());
    }
}
