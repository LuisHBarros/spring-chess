package com.chess.social.domain.repository;

import com.chess.social.domain.model.Guild;
import com.chess.social.domain.model.GuildId;
import com.chess.social.domain.model.GuildName;
import com.chess.social.domain.model.UserId;

import java.util.List;
import java.util.Optional;

public interface GuildRepository {
    Guild save(Guild guild);

    Optional<Guild> findById(GuildId id);

    Optional<Guild> findByName(GuildName name);

    boolean existsByName(GuildName name);

    List<Guild> findAllByMemberId(UserId userId);

    void delete(Guild guild);
}
