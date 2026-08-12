package com.chess.chat.domain;

import com.chess.chat.domain.exception.ChatRoomNotFoundException;
import com.chess.chat.domain.exception.DuplicateDirectChatException;
import com.chess.chat.domain.model.ChatRoom;
import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.ChatRoomStatus;
import com.chess.chat.domain.model.ParticipantRole;
import com.chess.chat.domain.model.RoomTitle;
import com.chess.chat.domain.model.UserId;
import com.chess.chat.domain.port.ChatEventPublisherPort;
import com.chess.chat.domain.repository.ChatRoomRepository;
import com.chess.chat.domain.repository.MessageRepository;
import com.chess.chat.domain.service.ChatRoomDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomDomainServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatEventPublisherPort eventPublisher;

    private ChatRoomDomainService chatRoomDomainService;
    private UserId user1;
    private UserId user2;
    private UserId user3;

    @BeforeEach
    void setUp() {
        chatRoomDomainService = new ChatRoomDomainService(chatRoomRepository, messageRepository, eventPublisher);
        user1 = UserId.generate();
        user2 = UserId.generate();
        user3 = UserId.generate();
    }

    @Test
    void shouldCreateDirectChatWhenNoneExists() {
        when(chatRoomRepository.findDirectRoomBetweenUsers(user1, user2)).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(inv -> inv.getArgument(0));

        ChatRoom result = chatRoomDomainService.createDirectChat(user1, user2);

        assertNotNull(result);
        assertTrue(result.isParticipant(user1));
        assertTrue(result.isParticipant(user2));
        verify(chatRoomRepository).save(any(ChatRoom.class));
        verify(eventPublisher).publishChatEvent(eq("CHAT_ROOM_CREATED"), anyString(), anyMap());
    }

    @Test
    void shouldThrowExceptionWhenDirectChatAlreadyExists() {
        ChatRoom existing = ChatRoom.createDirect(user1, user2);
        when(chatRoomRepository.findDirectRoomBetweenUsers(user1, user2)).thenReturn(Optional.of(existing));

        assertThrows(DuplicateDirectChatException.class, () -> chatRoomDomainService.createDirectChat(user1, user2));
        verify(chatRoomRepository, never()).save(any());
    }

    @Test
    void shouldCreateGroupChat() {
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(inv -> inv.getArgument(0));

        ChatRoom result = chatRoomDomainService.createGroupChat(RoomTitle.of("Group Chat"), user1, List.of(user2, user3));

        assertNotNull(result);
        assertEquals(3, result.getParticipants().size());
        verify(eventPublisher).publishChatEvent(eq("CHAT_ROOM_CREATED"), anyString(), anyMap());
    }

    @Test
    void shouldAddParticipantToRoom() {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("Group Chat"), user1, List.of(user2));
        when(chatRoomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(inv -> inv.getArgument(0));

        ChatRoom updated = chatRoomDomainService.addParticipant(room.getId(), user1, user3, ParticipantRole.MEMBER);

        assertTrue(updated.isParticipant(user3));
        verify(eventPublisher).publishChatEvent(eq("PARTICIPANT_ADDED"), eq(room.getId().toString()), anyMap());
    }

    @Test
    void shouldRemoveParticipantFromRoom() {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("Group Chat"), user1, List.of(user2, user3));
        when(chatRoomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(inv -> inv.getArgument(0));

        ChatRoom updated = chatRoomDomainService.removeParticipant(room.getId(), user1, user3);

        assertFalse(updated.isParticipant(user3));
        verify(eventPublisher).publishChatEvent(eq("PARTICIPANT_REMOVED"), eq(room.getId().toString()), anyMap());
    }

    @Test
    void shouldArchiveAndActivateRoom() {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("Group Chat"), user1, List.of(user2));
        when(chatRoomRepository.findById(room.getId())).thenReturn(Optional.of(room));
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(inv -> inv.getArgument(0));

        ChatRoom archived = chatRoomDomainService.archiveChatRoom(room.getId(), user1);
        assertEquals(ChatRoomStatus.ARCHIVED, archived.getStatus());

        ChatRoom activated = chatRoomDomainService.activateChatRoom(room.getId(), user1);
        assertEquals(ChatRoomStatus.ACTIVE, activated.getStatus());
    }

    @Test
    void shouldDeleteChatRoomAndAllMessages() {
        ChatRoom room = ChatRoom.createGroup(RoomTitle.of("Group Chat"), user1, List.of(user2));
        when(chatRoomRepository.findById(room.getId())).thenReturn(Optional.of(room));

        chatRoomDomainService.deleteChatRoom(room.getId(), user1);

        verify(messageRepository).deleteAllByChatRoomId(room.getId());
        verify(chatRoomRepository).delete(room);
        verify(eventPublisher).publishChatEvent(eq("CHAT_ROOM_DELETED"), eq(room.getId().toString()), anyMap());
    }
}
