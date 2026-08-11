package com.chess.auth.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("Should create User entity with correct properties")
    void shouldCreateUser() {
        Username username = new Username("grandmaster");
        Email email = new Email("gm@chess.com");
        Password password = Password.fromHash("hashedSecret");

        User user = User.create(username, email, password);

        assertNotNull(user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getLastSeenAt());
    }

    @Test
    @DisplayName("Should update lastSeenAt timestamp")
    void shouldUpdateLastSeenAt() throws InterruptedException {
        User user = User.create(
                new Username("player1"),
                new Email("p1@chess.com"),
                Password.fromHash("hash123")
        );

        Instant oldLastSeen = user.getLastSeenAt();
        Instant newLastSeen = oldLastSeen.plusSeconds(60);

        user.updateLastSeenAt(newLastSeen);

        assertEquals(newLastSeen, user.getLastSeenAt());
    }

    @Test
    @DisplayName("Should update user password")
    void shouldUpdatePassword() {
        User user = User.create(
                new Username("player1"),
                new Email("p1@chess.com"),
                Password.fromHash("oldHash")
        );

        Password newHash = Password.fromHash("newHashedPassword");
        user.changePassword(newHash);

        assertEquals(newHash, user.getPassword());
    }

    @Test
    @DisplayName("Should reconstitute existing User entity")
    void shouldReconstituteUser() {
        UserId id = UserId.generate();
        Username username = new Username("reconstituted");
        Email email = new Email("rec@chess.com");
        Password password = Password.fromHash("hash");
        Instant created = Instant.now().minusSeconds(3600);
        Instant lastSeen = Instant.now();

        User user = User.reconstitute(id, username, email, password, created, lastSeen);

        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(created, user.getCreatedAt());
        assertEquals(lastSeen, user.getLastSeenAt());
    }
}
