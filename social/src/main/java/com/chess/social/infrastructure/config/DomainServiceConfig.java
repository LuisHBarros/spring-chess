package com.chess.social.infrastructure.config;

import com.chess.social.domain.repository.FriendshipRepository;
import com.chess.social.domain.repository.GuildRepository;
import com.chess.social.domain.service.FriendshipDomainService;
import com.chess.social.domain.service.GuildDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public FriendshipDomainService friendshipDomainService(FriendshipRepository friendshipRepository) {
        return new FriendshipDomainService(friendshipRepository);
    }

    @Bean
    public GuildDomainService guildDomainService(GuildRepository guildRepository) {
        return new GuildDomainService(guildRepository);
    }
}
