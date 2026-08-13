package com.chess.chat.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MessageReactionTest {

    private UserId userId;

    @BeforeEach
    void setUp() {
        userId = UserId.generate();
    }

    @Test
    void shouldCreateMessageReactionWithEmoji() {
        MessageReaction reaction = MessageReaction.create(userId, " 🔥 ");

        assertEquals(userId, reaction.getUserId());
        assertEquals("🔥", reaction.getEmoji());
        assertNotNull(reaction.getCreatedAt());
    }

    @Test
    void shouldReconstituteMessageReaction() {
        Instant timestamp = Instant.now().minusSeconds(100);
        MessageReaction reaction = MessageReaction.reconstitute(userId, "❤️", timestamp);

        assertEquals(userId, reaction.getUserId());
        assertEquals("❤️", reaction.getEmoji());
        assertEquals(timestamp, reaction.getCreatedAt());
    }

    @Test
    void shouldThrowExceptionWhenCreatingWithInvalidArguments() {
        assertThrows(IllegalArgumentException.class, () -> MessageReaction.create(null, "👍"));
        assertThrows(IllegalArgumentException.class, () -> MessageReaction.create(userId, null));
        assertThrows(IllegalArgumentException.class, () -> MessageReaction.create(userId, "   "));
        assertThrows(IllegalArgumentException.class, () -> MessageReaction.reconstitute(userId, "👍", null));
    }

    @Test
    void shouldVerifyEqualsAndHashCode() {
        MessageReaction r1 = MessageReaction.create(userId, "🎉");
        MessageReaction r2 = MessageReaction.create(userId, "🎉");
        MessageReaction r3 = MessageReaction.create(userId, "👏");
        MessageReaction r4 = MessageReaction.create(UserId.generate(), "🎉");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        assertNotEquals(r1, r3);
        assertNotEquals(r1, r4);
        assertNotEquals(r1, null);
        assertNotEquals(r1, "String");
    }
}
