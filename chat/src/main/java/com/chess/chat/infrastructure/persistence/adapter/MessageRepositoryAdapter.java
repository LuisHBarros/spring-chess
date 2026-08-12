package com.chess.chat.infrastructure.persistence.adapter;

import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.Message;
import com.chess.chat.domain.model.MessageId;
import com.chess.chat.domain.model.UserId;
import com.chess.chat.domain.repository.MessageRepository;
import com.chess.chat.infrastructure.persistence.entity.MessageJpaEntity;
import com.chess.chat.infrastructure.persistence.repository.SpringDataMessageRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MessageRepositoryAdapter implements MessageRepository {

    private final SpringDataMessageRepository repository;

    public MessageRepositoryAdapter(SpringDataMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public Message save(Message message) {
        MessageJpaEntity entity = MessageJpaEntity.fromDomain(message);
        MessageJpaEntity saved = repository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Message> findById(MessageId id) {
        return repository.findById(id.getValue()).map(MessageJpaEntity::toDomain);
    }

    @Override
    public List<Message> findByChatRoomId(ChatRoomId chatRoomId, int page, int size) {
        return repository.findByChatRoomIdOrderBySentAtDesc(chatRoomId.getValue(), PageRequest.of(page, size)).stream()
                .map(MessageJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findUnreadMessages(ChatRoomId chatRoomId, UserId userId) {
        return repository.findUnreadMessages(chatRoomId.getValue(), userId.getValue()).stream()
                .map(MessageJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countUnreadMessages(ChatRoomId chatRoomId, UserId userId) {
        return repository.countUnreadMessages(chatRoomId.getValue(), userId.getValue());
    }

    @Override
    public void delete(Message message) {
        repository.deleteById(message.getId().getValue());
    }

    @Override
    public void deleteAllByChatRoomId(ChatRoomId chatRoomId) {
        repository.deleteAllByChatRoomId(chatRoomId.getValue());
    }
}
