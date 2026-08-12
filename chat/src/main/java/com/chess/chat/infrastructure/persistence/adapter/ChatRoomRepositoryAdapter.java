package com.chess.chat.infrastructure.persistence.adapter;

import com.chess.chat.domain.model.ChatRoom;
import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.UserId;
import com.chess.chat.domain.repository.ChatRoomRepository;
import com.chess.chat.infrastructure.persistence.entity.ChatRoomJpaEntity;
import com.chess.chat.infrastructure.persistence.repository.SpringDataChatRoomRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ChatRoomRepositoryAdapter implements ChatRoomRepository {

    private final SpringDataChatRoomRepository repository;

    public ChatRoomRepositoryAdapter(SpringDataChatRoomRepository repository) {
        this.repository = repository;
    }

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        ChatRoomJpaEntity entity = ChatRoomJpaEntity.fromDomain(chatRoom);
        ChatRoomJpaEntity saved = repository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<ChatRoom> findById(ChatRoomId id) {
        return repository.findById(id.getValue()).map(ChatRoomJpaEntity::toDomain);
    }

    @Override
    public Optional<ChatRoom> findDirectRoomBetweenUsers(UserId userA, UserId userB) {
        return repository.findDirectRoomBetweenUsers(userA.getValue(), userB.getValue())
                .map(ChatRoomJpaEntity::toDomain);
    }

    @Override
    public List<ChatRoom> findByParticipantUserId(UserId userId) {
        return repository.findByParticipantUserId(userId.getValue()).stream()
                .map(ChatRoomJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatRoom> findByTargetReferenceId(String targetReferenceId) {
        return repository.findByTargetReferenceId(targetReferenceId).stream()
                .map(ChatRoomJpaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(ChatRoom chatRoom) {
        repository.deleteById(chatRoom.getId().getValue());
    }
}
