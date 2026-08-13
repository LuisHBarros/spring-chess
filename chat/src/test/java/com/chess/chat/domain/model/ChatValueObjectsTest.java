package com.chess.chat.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ChatValueObjectsTest {

    @Test
    void shouldTestChatRoomIdBehavior() {
        UUID uuid = UUID.randomUUID();
        ChatRoomId id1 = ChatRoomId.from(uuid);
        ChatRoomId id2 = ChatRoomId.fromString(uuid.toString());
        ChatRoomId idGenerated = ChatRoomId.generate();

        assertEquals(uuid, id1.getValue());
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertEquals(uuid.toString(), id1.toString());
        assertNotEquals(id1, idGenerated);

        assertThrows(IllegalArgumentException.class, () -> ChatRoomId.from(null));
        assertThrows(IllegalArgumentException.class, () -> ChatRoomId.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> ChatRoomId.fromString("   "));
        assertThrows(IllegalArgumentException.class, () -> ChatRoomId.fromString("invalid-uuid"));
    }

    @Test
    void shouldTestMessageIdBehavior() {
        UUID uuid = UUID.randomUUID();
        MessageId id1 = MessageId.from(uuid);
        MessageId id2 = MessageId.fromString(uuid.toString());
        MessageId idGenerated = MessageId.generate();

        assertEquals(uuid, id1.getValue());
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertEquals(uuid.toString(), id1.toString());
        assertNotEquals(id1, idGenerated);

        assertThrows(IllegalArgumentException.class, () -> MessageId.from(null));
        assertThrows(IllegalArgumentException.class, () -> MessageId.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> MessageId.fromString("   "));
        assertThrows(IllegalArgumentException.class, () -> MessageId.fromString("not-a-uuid"));
    }

    @Test
    void shouldTestRoomTitleBehavior() {
        RoomTitle title1 = RoomTitle.of(" Chess Champions ");
        RoomTitle title2 = RoomTitle.of("Chess Champions");

        assertEquals("Chess Champions", title1.getValue());
        assertEquals(title1, title2);
        assertEquals(title1.hashCode(), title2.hashCode());
        assertEquals("Chess Champions", title1.toString());

        assertThrows(IllegalArgumentException.class, () -> RoomTitle.of(null));
        assertThrows(IllegalArgumentException.class, () -> RoomTitle.of(""));
        assertThrows(IllegalArgumentException.class, () -> RoomTitle.of("   "));

        String overMax = "a".repeat(RoomTitle.MAX_LENGTH + 1);
        assertThrows(IllegalArgumentException.class, () -> RoomTitle.of(overMax));

        String maxAllowed = "a".repeat(RoomTitle.MAX_LENGTH);
        assertDoesNotThrow(() -> RoomTitle.of(maxAllowed));
    }
}
