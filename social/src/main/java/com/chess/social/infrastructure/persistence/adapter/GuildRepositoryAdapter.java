package com.chess.social.infrastructure.persistence.adapter;

import com.chess.social.domain.model.Guild;
import com.chess.social.domain.model.GuildId;
import com.chess.social.domain.model.GuildName;
import com.chess.social.domain.model.UserId;
import com.chess.social.domain.repository.GuildRepository;
import com.chess.social.infrastructure.persistence.entity.GuildJpaEntity;
import com.chess.social.infrastructure.persistence.repository.SpringDataGuildRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GuildRepositoryAdapter implements GuildRepository {

    private final SpringDataGuildRepository repository;

    public GuildRepositoryAdapter(SpringDataGuildRepository repository) {
        this.repository = repository;
    }

    @Override
    public Guild save(Guild guild) {
        GuildJpaEntity entity = GuildJpaEntity.fromDomain(guild);
        GuildJpaEntity saved = repository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Guild> findById(GuildId id) {
        return repository.findById(id.getValue()).map(GuildJpaEntity::toDomain);
    }

    @Override
    public Optional<Guild> findByName(GuildName name) {
        return repository.findByName(name.getValue()).map(GuildJpaEntity::toDomain);
    }

    @Override
    public boolean existsByName(GuildName name) {
        return repository.existsByName(name.getValue());
    }

    @Override
    public List<Guild> findAllByMemberId(UserId userId) {
        return repository.findAllByMemberId(userId.getValue()).stream()
                .map(GuildJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Guild guild) {
        repository.deleteById(guild.getId().getValue());
    }
}
