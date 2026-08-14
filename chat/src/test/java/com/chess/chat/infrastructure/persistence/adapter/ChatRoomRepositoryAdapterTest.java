package com.chess.chat.infrastructure.persistence.adapter;

import com.chess.chat.domain.model.ChatRoom;
import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.RoomTitle;
import com.chess.chat.domain.model.UserId;
import com.chess.chat.infrastructure.persistence.entity.ChatRoomJpaEntity;
import com.chess.chat.infrastructure.persistence.repository.SpringDataChatRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomRepositoryAdapterTest {

    @Mock
    private SpringDataChatRoomRepository repository;

    @InjectMocks
    private ChatRoomRepositoryAdapter adapter;

    private UserId user1;
    private UserId user2;
    private ChatRoom room;
    private ChatRoomJpaEntity entity;

    @BeforeEach
    void setUp() {
        user1 = UserId.generate();
        user2 = UserId.generate();
        room = ChatRoom.createDirect(user1, user2);
        entity = ChatRoomJpaEntity.fromDomain(room);
    }

    @Test
    void shouldSaveChatRoom() {
        when(repository.saveAndFlush(any(ChatRoomJpaEntity.class))).thenReturn(entity);

        ChatRoom savedRoom = adapter.save(room);

        assertNotNull(savedRoom);
        assertEquals(room.getId(), savedRoom.getId());
        assertEquals(room.getType(), savedRoom.getType());
        verify(repository).saveAndFlush(any(ChatRoomJpaEntity.class));
    }

    @Test
    void shouldFindByIdWhenPresent() {
        when(repository.findById(room.getId().getValue())).thenReturn(Optional.of(entity));

        Optional<ChatRoom> result = adapter.findById(room.getId());

        assertTrue(result.isPresent());
        assertEquals(room.getId(), result.get().getId());
        verify(repository).findById(room.getId().getValue());
    }

    @Test
    void shouldFindByIdWhenEmpty() {
        ChatRoomId missingId = ChatRoomId.generate();
        when(repository.findById(missingId.getValue())).thenReturn(Optional.empty());

        Optional<ChatRoom> result = adapter.findById(missingId);

        assertTrue(result.isEmpty());
        verify(repository).findById(missingId.getValue());
    }

    @Test
    void shouldFindDirectRoomBetweenUsers() {
        when(repository.findDirectRoomBetweenUsers(user1.getValue(), user2.getValue()))
                .thenReturn(Optional.of(entity));

        Optional<ChatRoom> result = adapter.findDirectRoomBetweenUsers(user1, user2);

        assertTrue(result.isPresent());
        assertEquals(room.getId(), result.get().getId());
        verify(repository).findDirectRoomBetweenUsers(user1.getValue(), user2.getValue());
    }

    @Test
    void shouldFindByParticipantUserId() {
        when(repository.findByParticipantUserId(user1.getValue()))
                .thenReturn(List.of(entity));

        List<ChatRoom> results = adapter.findByParticipantUserId(user1);

        assertEquals(1, results.size());
        assertEquals(room.getId(), results.get(0).getId());
        verify(repository).findByParticipantUserId(user1.getValue());
    }

    @Test
    void shouldFindByTargetReferenceId() {
        ChatRoom guildRoom = ChatRoom.createGuildChannel(RoomTitle.of("Guild Chat"), user1, "guild-777");
        ChatRoomJpaEntity guildEntity = ChatRoomJpaEntity.fromDomain(guildRoom);

        when(repository.findByTargetReferenceId("guild-777"))
                .thenReturn(List.of(guildEntity));

        List<ChatRoom> results = adapter.findByTargetReferenceId("guild-777");

        assertEquals(1, results.size());
        assertEquals("guild-777", results.get(0).getTargetReferenceId());
        verify(repository).findByTargetReferenceId("guild-777");
    }

    @Test
    void shouldDeleteChatRoom() {
        doNothing().when(repository).deleteById(room.getId().getValue());

        adapter.delete(room);

        verify(repository).deleteById(room.getId().getValue());
    }
}
