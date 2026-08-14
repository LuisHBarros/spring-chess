package com.chess.chat.infrastructure.persistence.entity;

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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MessageJpaEntityTest {

    private ChatRoomId chatRoomId;
    private UserId sender;

    @BeforeEach
    void setUp() {
        chatRoomId = ChatRoomId.generate();
        sender = UserId.generate();
    }

    @Test
    void shouldMapTextMessageToEntityAndBack() {
        Message domainMessage = Message.send(chatRoomId, sender, MessageContent.of("Checkmate!"), MessageType.TEXT, null);
        domainMessage.addReaction(sender, "🎉");

        MessageJpaEntity entity = MessageJpaEntity.fromDomain(domainMessage);

        assertNotNull(entity);
        assertEquals(domainMessage.getId().getValue(), entity.getId());
        assertEquals(chatRoomId.getValue(), entity.getChatRoomId());
        assertEquals(sender.getValue(), entity.getSenderId());
        assertEquals("Checkmate!", entity.getContent());
        assertEquals(MessageType.TEXT, entity.getType());
        assertEquals(MessageStatus.SENT, entity.getStatus());
        assertNull(entity.getReplyToMessageId());
        assertEquals(1, entity.getReactions().size());
        assertEquals("🎉", entity.getReactions().get(0).getEmoji());
        assertFalse(entity.isDeleted());
        assertNotNull(entity.getSentAt());
        assertEquals(0L, entity.getSequence());

        Message reconstituted = entity.toDomain();
        assertEquals(domainMessage.getId(), reconstituted.getId());
        assertEquals(domainMessage.getChatRoomId(), reconstituted.getChatRoomId());
        assertEquals(domainMessage.getSenderId(), reconstituted.getSenderId());
        assertEquals("Checkmate!", reconstituted.getContent().getValue());
        assertEquals(MessageType.TEXT, reconstituted.getType());
        assertEquals(1, reconstituted.getReactions().size());
        assertEquals("🎉", reconstituted.getReactions().get(0).getEmoji());
        assertEquals(0L, reconstituted.getSequence());
    }

    @Test
    void shouldMapSystemMessageWithReplyToId() {
        MessageId parentId = MessageId.generate();
        Message domainMessage = Message.send(chatRoomId, sender, MessageContent.of("e4 e5"), MessageType.MOVE_SHARE, parentId);

        MessageJpaEntity entity = MessageJpaEntity.fromDomain(domainMessage);

        assertEquals(MessageType.MOVE_SHARE, entity.getType());
        assertEquals(parentId.getValue(), entity.getReplyToMessageId());
        assertEquals(0L, entity.getSequence());

        Message reconstituted = entity.toDomain();
        assertEquals(MessageType.MOVE_SHARE, reconstituted.getType());
        assertEquals(parentId, reconstituted.getReplyToMessageId());
        assertEquals(0L, reconstituted.getSequence());
    }

    @Test
    void shouldConstructWithFullConstructorAndGetters() {
        UUID msgId = UUID.randomUUID();
        Instant now = Instant.now();
        MessageJpaEntity entity = new MessageJpaEntity(
                msgId,
                chatRoomId.getValue(),
                sender.getValue(),
                "System notification",
                MessageType.SYSTEM,
                MessageStatus.DELIVERED,
                null,
                null,
                now,
                now,
                true,
                42L
        );

        assertEquals(msgId, entity.getId());
        assertEquals(chatRoomId.getValue(), entity.getChatRoomId());
        assertEquals(sender.getValue(), entity.getSenderId());
        assertEquals("System notification", entity.getContent());
        assertEquals(MessageType.SYSTEM, entity.getType());
        assertEquals(MessageStatus.DELIVERED, entity.getStatus());
        assertTrue(entity.getReactions().isEmpty());
        assertEquals(now, entity.getSentAt());
        assertEquals(now, entity.getEditedAt());
        assertTrue(entity.isDeleted());
        assertEquals(42L, entity.getSequence());
    }
}
