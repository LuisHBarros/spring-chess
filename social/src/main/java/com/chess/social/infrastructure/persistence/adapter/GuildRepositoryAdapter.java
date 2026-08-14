package com.chess.social.infrastructure.persistence.adapter;

import com.chess.social.domain.model.Guild;
import com.chess.social.domain.model.GuildId;
import com.chess.social.domain.model.GuildName;
import com.chess.social.domain.model.UserId;
import com.chess.social.domain.repository.GuildRepository;
import com.chess.social.infrastructure.persistence.entity.GuildJpaEntity;
import com.chess.social.infrastructure.persistence.repository.SpringDataGuildRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GuildRepositoryAdapter implements GuildRepository {

    private final SpringDataGuildRepository repository;

    public GuildRepositoryAdapter(SpringDataGuildRepository repository) {
        this.repository = repository;
    }

    @Override
    public Guild save(Guild guild) {
        GuildJpaEntity entity = GuildJpaEntity.fromDomain(guild);
        GuildJpaEntity saved = repository.saveAndFlush(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Guild> findById(GuildId id) {
        return repository.findByIdWithDetails(id.getValue()).map(GuildJpaEntity::toDomain);
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
    public Page<Guild> findAllByMemberId(UserId userId, Pageable pageable) {
        return repository.findAllByMemberId(userId.getValue(), pageable)
                .map(GuildJpaEntity::toDomain);
    }

    @Override
    public void delete(Guild guild) {
        repository.deleteById(guild.getId().getValue());
    }
}
