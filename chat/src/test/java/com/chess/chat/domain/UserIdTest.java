package com.chess.chat.domain;

import com.chess.chat.domain.model.UserId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserIdTest {

    @Test
    void shouldGenerateValidUserId() {
        UserId userId = UserId.generate();
        assertNotNull(userId);
        assertNotNull(userId.getValue());
    }

    @Test
    void shouldCreateUserIdFromUUID() {
        UUID uuid = UUID.randomUUID();
        UserId userId = UserId.from(uuid);
        assertEquals(uuid, userId.getValue());
    }

    @Test
    void shouldCreateUserIdFromString() {
        UUID uuid = UUID.randomUUID();
        UserId userId = UserId.fromString(uuid.toString());
        assertEquals(uuid, userId.getValue());
    }

    @Test
    void shouldThrowExceptionWhenNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> UserId.from(null));
        assertThrows(IllegalArgumentException.class, () -> UserId.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> UserId.fromString("   "));
    }

    @Test
    void shouldTestEquality() {
        UUID uuid = UUID.randomUUID();
        UserId user1 = UserId.from(uuid);
        UserId user2 = UserId.from(uuid);
        UserId user3 = UserId.generate();

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1, user3);
        assertEquals(uuid.toString(), user1.toString());
    }
}
