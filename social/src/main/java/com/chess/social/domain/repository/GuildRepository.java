package com.chess.social.domain.repository;

import com.chess.social.domain.model.Guild;
import com.chess.social.domain.model.GuildId;
import com.chess.social.domain.model.GuildName;
import com.chess.social.domain.model.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface GuildRepository {
    Guild save(Guild guild);

    Optional<Guild> findById(GuildId id);

    Optional<Guild> findByName(GuildName name);

    boolean existsByName(GuildName name);

    Page<Guild> findAllByMemberId(UserId userId, Pageable pageable);

    void delete(Guild guild);
}
