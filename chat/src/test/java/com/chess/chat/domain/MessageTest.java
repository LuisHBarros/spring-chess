package com.chess.chat.domain;

import com.chess.chat.domain.exception.UnauthorizedChatOperationException;
import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.Message;
import com.chess.chat.domain.model.MessageContent;
import com.chess.chat.domain.model.MessageId;
import com.chess.chat.domain.model.MessageStatus;
import com.chess.chat.domain.model.MessageType;
import com.chess.chat.domain.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
        assertEquals(0L, message.getSequence());
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

    @Test
    void shouldSendMessageWithExplicitSequence() {
        Message message = Message.send(roomId, senderId, MessageContent.of("Ordered"), MessageType.TEXT, null, 5L);

        assertEquals(5L, message.getSequence());
    }

    @Test
    void shouldReconstituteWithExplicitSequence() {
        Message message = Message.reconstitute(
                MessageId.generate(),
                roomId,
                senderId,
                MessageContent.of("Reconstituted"),
                MessageType.TEXT,
                MessageStatus.SENT,
                null,
                new ArrayList<>(),
                Instant.now(),
                null,
                false,
                7L
        );

        assertEquals(7L, message.getSequence());
    }

    @Test
    void shouldOrderMessagesBySentAtAndSequence() {
        Instant now = Instant.now();

        Message first = Message.reconstitute(
                MessageId.generate(),
                roomId,
                senderId,
                MessageContent.of("First"),
                MessageType.TEXT,
                MessageStatus.SENT,
                null,
                new ArrayList<>(),
                now,
                null,
                false,
                1L
        );

        Message second = Message.reconstitute(
                MessageId.generate(),
                roomId,
                senderId,
                MessageContent.of("Second"),
                MessageType.TEXT,
                MessageStatus.SENT,
                null,
                new ArrayList<>(),
                now,
                null,
                false,
                2L
        );

        Message third = Message.reconstitute(
                MessageId.generate(),
                roomId,
                senderId,
                MessageContent.of("Third"),
                MessageType.TEXT,
                MessageStatus.SENT,
                null,
                new ArrayList<>(),
                now,
                null,
                false,
                3L
        );

        List<Message> messages = new ArrayList<>(List.of(third, first, second));
        messages.sort(Comparator.comparing(Message::getSentAt).thenComparingLong(Message::getSequence));

        assertEquals(first, messages.get(0));
        assertEquals(second, messages.get(1));
        assertEquals(third, messages.get(2));
    }
}
