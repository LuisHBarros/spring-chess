package com.chess.auth.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserIdTest {

    @Test
    @DisplayName("Should generate a valid random UserId")
    void shouldGenerateValidUserId() {
        UserId id = UserId.generate();
        assertNotNull(id);
        assertNotNull(id.getValue());
    }

    @Test
    @DisplayName("Should create UserId from valid UUID object")
    void shouldCreateFromUUID() {
        UUID uuid = UUID.randomUUID();
        UserId id = UserId.from(uuid);
        assertEquals(uuid, id.getValue());
    }

    @Test
    @DisplayName("Should create UserId from valid UUID string")
    void shouldCreateFromUUIDString() {
        UUID uuid = UUID.randomUUID();
        UserId id = UserId.fromString(uuid.toString());
        assertEquals(uuid, id.getValue());
    }

    @Test
    @DisplayName("Should throw exception when creating from null UUID")
    void shouldThrowExceptionWhenNullUUID() {
        assertThrows(IllegalArgumentException.class, () -> UserId.from(null));
    }

    @Test
    @DisplayName("Should throw exception when creating from invalid UUID string")
    void shouldThrowExceptionWhenInvalidString() {
        assertThrows(IllegalArgumentException.class, () -> UserId.fromString("not-a-uuid"));
    }

    @Test
    @DisplayName("Should satisfy equality based on value")
    void shouldSatisfyEquality() {
        UUID uuid = UUID.randomUUID();
        UserId id1 = UserId.from(uuid);
        UserId id2 = UserId.from(uuid);
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }
}
