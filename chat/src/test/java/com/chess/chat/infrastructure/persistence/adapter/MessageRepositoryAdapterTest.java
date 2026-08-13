package com.chess.chat.infrastructure.persistence.adapter;

import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.Message;
import com.chess.chat.domain.model.MessageContent;
import com.chess.chat.domain.model.MessageId;
import com.chess.chat.domain.model.MessageType;
import com.chess.chat.domain.model.UserId;
import com.chess.chat.infrastructure.persistence.entity.MessageJpaEntity;
import com.chess.chat.infrastructure.persistence.repository.SpringDataMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageRepositoryAdapterTest {

    @Mock
    private SpringDataMessageRepository repository;

    @InjectMocks
    private MessageRepositoryAdapter adapter;

    private ChatRoomId chatRoomId;
    private UserId sender;
    private Message message;
    private MessageJpaEntity entity;

    @BeforeEach
    void setUp() {
        chatRoomId = ChatRoomId.generate();
        sender = UserId.generate();
        message = Message.create(chatRoomId, sender, MessageContent.of("Hello world"), MessageType.TEXT, null);
        entity = MessageJpaEntity.fromDomain(message);
    }

    @Test
    void shouldSaveMessage() {
        when(repository.save(any(MessageJpaEntity.class))).thenReturn(entity);

        Message saved = adapter.save(message);

        assertNotNull(saved);
        assertEquals(message.getId(), saved.getId());
        assertEquals("Hello world", saved.getContent().getValue());
        verify(repository).save(any(MessageJpaEntity.class));
    }

    @Test
    void shouldFindByIdWhenPresent() {
        when(repository.findById(message.getId().getValue())).thenReturn(Optional.of(entity));

        Optional<Message> result = adapter.findById(message.getId());

        assertTrue(result.isPresent());
        assertEquals(message.getId(), result.get().getId());
        verify(repository).findById(message.getId().getValue());
    }

    @Test
    void shouldFindByIdWhenEmpty() {
        MessageId missingId = MessageId.generate();
        when(repository.findById(missingId.getValue())).thenReturn(Optional.empty());

        Optional<Message> result = adapter.findById(missingId);

        assertTrue(result.isEmpty());
        verify(repository).findById(missingId.getValue());
    }

    @Test
    void shouldFindByChatRoomIdWithPagination() {
        when(repository.findByChatRoomIdOrderBySentAtDesc(eq(chatRoomId.getValue()), eq(PageRequest.of(0, 10))))
                .thenReturn(List.of(entity));

        List<Message> results = adapter.findByChatRoomId(chatRoomId, 0, 10);

        assertEquals(1, results.size());
        assertEquals(message.getId(), results.get(0).getId());
        verify(repository).findByChatRoomIdOrderBySentAtDesc(eq(chatRoomId.getValue()), eq(PageRequest.of(0, 10)));
    }

    @Test
    void shouldFindUnreadMessages() {
        when(repository.findUnreadMessages(chatRoomId.getValue(), sender.getValue()))
                .thenReturn(List.of(entity));

        List<Message> unread = adapter.findUnreadMessages(chatRoomId, sender);

        assertEquals(1, unread.size());
        assertEquals(message.getId(), unread.get(0).getId());
        verify(repository).findUnreadMessages(chatRoomId.getValue(), sender.getValue());
    }

    @Test
    void shouldCountUnreadMessages() {
        when(repository.countUnreadMessages(chatRoomId.getValue(), sender.getValue()))
                .thenReturn(3L);

        long count = adapter.countUnreadMessages(chatRoomId, sender);

        assertEquals(3L, count);
        verify(repository).countUnreadMessages(chatRoomId.getValue(), sender.getValue());
    }

    @Test
    void shouldDeleteMessage() {
        doNothing().when(repository).deleteById(message.getId().getValue());

        adapter.delete(message);

        verify(repository).deleteById(message.getId().getValue());
    }

    @Test
    void shouldDeleteAllByChatRoomId() {
        doNothing().when(repository).deleteAllByChatRoomId(chatRoomId.getValue());

        adapter.deleteAllByChatRoomId(chatRoomId);

        verify(repository).deleteAllByChatRoomId(chatRoomId.getValue());
    }
}
