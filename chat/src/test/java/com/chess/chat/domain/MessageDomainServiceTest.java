package com.chess.chat.domain;

import com.chess.chat.domain.exception.ChatRoomArchivedException;
import com.chess.chat.domain.exception.ChatRoomNotFoundException;
import com.chess.chat.domain.exception.MessageNotFoundException;
import com.chess.chat.domain.exception.UnauthorizedChatOperationException;
import com.chess.chat.domain.model.ChatRoom;
import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.Message;
import com.chess.chat.domain.model.MessageContent;
import com.chess.chat.domain.model.MessageId;
import com.chess.chat.domain.model.MessageStatus;
import com.chess.chat.domain.model.MessageType;
import com.chess.chat.domain.model.RoomTitle;
import com.chess.chat.domain.model.UserId;
import com.chess.chat.domain.model.ChatRoomType;
import com.chess.chat.domain.port.ChatEventPublisherPort;
import com.chess.chat.domain.port.GuildPermissionPort;
import com.chess.chat.domain.repository.ChatRoomRepository;
import com.chess.chat.domain.repository.MessageRepository;
import com.chess.chat.domain.service.MessageDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageDomainServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatEventPublisherPort eventPublisher;

    @Mock
    private GuildPermissionPort guildPermissionPort;

    private MessageDomainService messageDomainService;
    private UserId user1;
    private UserId user2;
    private UserId outsider;
    private ChatRoom activeRoom;

    @BeforeEach
    void setUp() {
        messageDomainService = new MessageDomainService(chatRoomRepository, messageRepository, eventPublisher, guildPermissionPort);
        user1 = UserId.generate();
        user2 = UserId.generate();
        outsider = UserId.generate();
        activeRoom = ChatRoom.createGroup(RoomTitle.of("Test Room"), user1, List.of(user2));
    }

    @Test
    void shouldSendMessageSuccessfully() {
        when(chatRoomRepository.findById(activeRoom.getId())).thenReturn(Optional.of(activeRoom));
        when(messageRepository.findTopByChatRoomIdOrderBySequenceDesc(activeRoom.getId())).thenReturn(Optional.empty());
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        Message message = messageDomainService.sendMessage(activeRoom.getId(), user1, MessageContent.of("e4 e5"));

        assertNotNull(message);
        assertEquals(activeRoom.getId(), message.getChatRoomId());
        assertEquals(user1, message.getSenderId());
        assertEquals("e4 e5", message.getContent().getValue());
        assertEquals(1L, message.getSequence());
        verify(eventPublisher).publishChatEvent(eq("MESSAGE_SENT"), anyString(), anyMap());
    }

    @Test
    void shouldSendMessageInGuildChannelWhenHasChatAccess() {
        ChatRoom guildRoom = ChatRoom.createGuildChannel(RoomTitle.of("Guild Chat"), user1, "guild-123");
        when(chatRoomRepository.findById(guildRoom.getId())).thenReturn(Optional.of(guildRoom));
        when(guildPermissionPort.hasChatAccess(user1, "guild-123")).thenReturn(true);
        when(messageRepository.findTopByChatRoomIdOrderBySequenceDesc(guildRoom.getId())).thenReturn(Optional.empty());
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        Message message = messageDomainService.sendMessage(guildRoom.getId(), user1, MessageContent.of("Hello guild"));

        assertNotNull(message);
        assertEquals(ChatRoomType.GUILD, guildRoom.getType());
        assertEquals(1L, message.getSequence());
        verify(guildPermissionPort).hasChatAccess(user1, "guild-123");
    }

    @Test
    void shouldThrowExceptionWhenSenderLacksGuildChatAccess() {
        ChatRoom guildRoom = ChatRoom.createGuildChannel(RoomTitle.of("Guild Chat"), user1, "guild-123");
        when(chatRoomRepository.findById(guildRoom.getId())).thenReturn(Optional.of(guildRoom));
        when(guildPermissionPort.hasChatAccess(user1, "guild-123")).thenReturn(false);

        assertThrows(UnauthorizedChatOperationException.class, () ->
                messageDomainService.sendMessage(guildRoom.getId(), user1, MessageContent.of("Hello")));
    }

    @Test
    void shouldThrowExceptionWhenSenderIsNotParticipant() {
        when(chatRoomRepository.findById(activeRoom.getId())).thenReturn(Optional.of(activeRoom));

        assertThrows(UnauthorizedChatOperationException.class, () ->
                messageDomainService.sendMessage(activeRoom.getId(), outsider, MessageContent.of("Hello")));
    }

    @Test
    void shouldThrowExceptionWhenRoomIsArchived() {
        activeRoom.archive(user1);
        when(chatRoomRepository.findById(activeRoom.getId())).thenReturn(Optional.of(activeRoom));

        assertThrows(ChatRoomArchivedException.class, () ->
                messageDomainService.sendMessage(activeRoom.getId(), user1, MessageContent.of("Hello")));
    }

    @Test
    void shouldEditMessageSuccessfully() {
        Message message = Message.send(activeRoom.getId(), user1, MessageContent.of("Original text"));
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        Message edited = messageDomainService.editMessage(message.getId(), user1, MessageContent.of("Updated text"));

        assertEquals("Updated text", edited.getContent().getValue());
        assertEquals(MessageStatus.EDITED, edited.getStatus());
        verify(eventPublisher).publishChatEvent(eq("MESSAGE_EDITED"), eq(message.getId().toString()), anyMap());
    }

    @Test
    void shouldDeleteMessageSuccessfully() {
        Message message = Message.send(activeRoom.getId(), user1, MessageContent.of("Bad message"));
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        Message deleted = messageDomainService.deleteMessage(message.getId(), user1);

        assertTrue(deleted.isDeleted());
        assertEquals(MessageStatus.DELETED, deleted.getStatus());
        verify(eventPublisher).publishChatEvent(eq("MESSAGE_DELETED"), eq(message.getId().toString()), anyMap());
    }

    @Test
    void shouldAddAndRemoveReaction() {
        Message message = Message.send(activeRoom.getId(), user1, MessageContent.of("Nice game"));
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));
        when(chatRoomRepository.findById(activeRoom.getId())).thenReturn(Optional.of(activeRoom));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        Message reacted = messageDomainService.addReaction(message.getId(), user2, "🔥");
        assertEquals(1, reacted.getReactions().size());

        Message unreacted = messageDomainService.removeReaction(message.getId(), user2, "🔥");
        assertEquals(0, unreacted.getReactions().size());
    }

    @Test
    void shouldMarkMessagesAsRead() {
        Message unreadMessage = Message.send(activeRoom.getId(), user1, MessageContent.of("Unread"));
        when(chatRoomRepository.findById(activeRoom.getId())).thenReturn(Optional.of(activeRoom));
        when(messageRepository.findUnreadMessages(activeRoom.getId(), user2)).thenReturn(List.of(unreadMessage));

        messageDomainService.markMessagesAsRead(activeRoom.getId(), user2);

        verify(chatRoomRepository).save(activeRoom);
        verify(messageRepository).save(unreadMessage);
        assertEquals(MessageStatus.READ, unreadMessage.getStatus());
    }

    @Test
    void shouldAssignNextSequenceBasedOnLatestMessage() {
        Message previous = Message.send(activeRoom.getId(), user1, MessageContent.of("Previous"), MessageType.TEXT, null, 3L);
        when(chatRoomRepository.findById(activeRoom.getId())).thenReturn(Optional.of(activeRoom));
        when(messageRepository.findTopByChatRoomIdOrderBySequenceDesc(activeRoom.getId())).thenReturn(Optional.of(previous));
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> inv.getArgument(0));

        Message message = messageDomainService.sendMessage(activeRoom.getId(), user1, MessageContent.of("Next"));

        assertEquals(4L, message.getSequence());
    }
}
