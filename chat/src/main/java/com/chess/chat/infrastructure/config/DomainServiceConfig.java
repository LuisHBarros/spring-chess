package com.chess.chat.infrastructure.config;

import com.chess.chat.domain.port.ChatEventPublisherPort;
import com.chess.chat.domain.port.GuildPermissionPort;
import com.chess.chat.domain.repository.ChatRoomRepository;
import com.chess.chat.domain.repository.MessageRepository;
import com.chess.chat.domain.service.ChatRoomDomainService;
import com.chess.chat.domain.service.MessageDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainServiceConfig {

    @Bean
    public ChatRoomDomainService chatRoomDomainService(
            ChatRoomRepository chatRoomRepository,
            MessageRepository messageRepository,
            ChatEventPublisherPort eventPublisher) {
        return new ChatRoomDomainService(chatRoomRepository, messageRepository, eventPublisher);
    }

    @Bean
    public MessageDomainService messageDomainService(
            ChatRoomRepository chatRoomRepository,
            MessageRepository messageRepository,
            ChatEventPublisherPort eventPublisher,
            GuildPermissionPort guildPermissionPort) {
        return new MessageDomainService(chatRoomRepository, messageRepository, eventPublisher, guildPermissionPort);
    }
}
