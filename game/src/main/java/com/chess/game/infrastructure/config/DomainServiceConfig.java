package com.chess.game.infrastructure.config;

import com.chess.game.domain.port.GameEventPublisherPort;
import com.chess.game.domain.repository.GameHistoryRepository;
import com.chess.game.domain.repository.GameRepository;
import com.chess.game.domain.service.GameDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {
    @Bean
    public GameDomainService gameDomainService(
            GameRepository gameRepository,
            GameEventPublisherPort eventPublisher,
            GameHistoryRepository historyRepository) {
        return new GameDomainService(gameRepository, eventPublisher, historyRepository);
    }
}
