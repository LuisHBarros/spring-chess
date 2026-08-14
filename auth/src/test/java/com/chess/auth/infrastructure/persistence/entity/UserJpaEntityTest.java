package com.chess.auth.infrastructure.persistence.entity;

import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.Password;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.model.Username;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserJpaEntityTest {

    @Test
    @DisplayName("Should convert domain User to UserJpaEntity and back")
    void shouldConvertDomainToEntityAndBack() {
        Username username = new Username("jpa_user");
        Email email = new Email("jpa@chess.com");
        Password password = Password.fromHash("hashed_123");

        User user = User.create(username, email, password);

        UserJpaEntity entity = UserJpaEntity.fromDomain(user);

        assertEquals(user.getId().getValue(), entity.getId());
        assertEquals(username.getValue(), entity.getUsername());
        assertEquals(email.getValue(), entity.getEmail());
        assertEquals(password.getValue(), entity.getPassword());
        assertEquals(user.getCreatedAt(), entity.getCreatedAt());
        assertEquals(user.getLastSeenAt(), entity.getLastSeenAt());
        assertEquals(user.getRefreshTokenVersion(), entity.getRefreshTokenVersion());

        User convertedDomain = entity.toDomain();
        assertEquals(user.getId(), convertedDomain.getId());
        assertEquals(username, convertedDomain.getUsername());
        assertEquals(email, convertedDomain.getEmail());
        assertEquals(password, convertedDomain.getPassword());
    }

    @Test
    @DisplayName("Should support getters and setters")
    void shouldSupportGettersAndSetters() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(id);
        entity.setUsername("uname");
        entity.setEmail("u@chess.com");
        entity.setPassword("pass");
        entity.setCreatedAt(now);
        entity.setLastSeenAt(now);
        entity.setRefreshTokenVersion(3);

        assertEquals(id, entity.getId());
        assertEquals("uname", entity.getUsername());
        assertEquals("u@chess.com", entity.getEmail());
        assertEquals("pass", entity.getPassword());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, entity.getLastSeenAt());
        assertEquals(3, entity.getRefreshTokenVersion());
    }
}
