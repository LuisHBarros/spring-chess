package com.chess.chat.domain;

import com.chess.chat.domain.exception.UnauthorizedChatOperationException;
import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.Message;
import com.chess.chat.domain.model.MessageContent;
import com.chess.chat.domain.model.MessageStatus;
import com.chess.chat.domain.model.MessageType;
import com.chess.chat.domain.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    private ChatRoomId roomId;
    private UserId senderId;
    private UserId otherUser;

    @BeforeEach
    void setUp() {
        roomId = ChatRoomId.generate();
        senderId = UserId.generate();
        otherUser = UserId.generate();
    }

    @Test
    void shouldSendMessage() {
        Message message = Message.send(roomId, senderId, MessageContent.of("Checkmate in 3 moves!"));

        assertNotNull(message.getId());
        assertEquals(roomId, message.getChatRoomId());
        assertEquals(senderId, message.getSenderId());
        assertEquals("Checkmate in 3 moves!", message.getContent().getValue());
        assertEquals(MessageType.TEXT, message.getType());
        assertEquals(MessageStatus.SENT, message.getStatus());
        assertFalse(message.isDeleted());
    }

    @Test
    void shouldEditMessageBySender() {
        Message message = Message.send(roomId, senderId, MessageContent.of("Initial message"));
        message.edit(senderId, MessageContent.of("Edited message"));

        assertEquals("Edited message", message.getContent().getValue());
        assertEquals(MessageStatus.EDITED, message.getStatus());
        assertNotNull(message.getEditedAt());
    }

    @Test
    void shouldThrowExceptionWhenNonSenderEditsMessage() {
        Message message = Message.send(roomId, senderId, MessageContent.of("Initial message"));
        assertThrows(UnauthorizedChatOperationException.class, () -> message.edit(otherUser, MessageContent.of("Hack")));
    }

    @Test
    void shouldDeleteMessage() {
        Message message = Message.send(roomId, senderId, MessageContent.of("Secret message"));
        message.delete(senderId);

        assertTrue(message.isDeleted());
        assertEquals(MessageStatus.DELETED, message.getStatus());
        assertEquals("[Message Deleted]", message.getContent().getValue());
    }

    @Test
    void shouldAddAndRemoveReactions() {
        Message message = Message.send(roomId, senderId, MessageContent.of("Great move!"));
        message.addReaction(otherUser, "👍");

        assertEquals(1, message.getReactions().size());
        assertEquals("👍", message.getReactions().get(0).getEmoji());

        // Duplicate reaction should not add twice
        message.addReaction(otherUser, "👍");
        assertEquals(1, message.getReactions().size());

        message.removeReaction(otherUser, "👍");
        assertEquals(0, message.getReactions().size());
    }

    @Test
    void shouldUpdateMessageStatus() {
        Message message = Message.send(roomId, senderId, MessageContent.of("Hello"));
        message.markAsDelivered();
        assertEquals(MessageStatus.DELIVERED, message.getStatus());

        message.markAsRead();
        assertEquals(MessageStatus.READ, message.getStatus());
    }
}
