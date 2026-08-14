package com.chess.auth.infrastructure.persistence.adapter;

import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.Password;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.model.Username;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(UserRepositoryAdapter.class)
class UserRepositoryAdapterTest {

    @Autowired
    private UserRepositoryAdapter userRepositoryAdapter;

    @Test
    @DisplayName("Should save and retrieve domain user entity via JPA adapter")
    void shouldSaveAndFindUser() {
        Username username = new Username("infra_user");
        Email email = new Email("infra@chess.com");
        Password password = Password.fromHash("hashed_pass_123");

        User user = User.create(username, email, password);

        User savedUser = userRepositoryAdapter.save(user);
        assertNotNull(savedUser);
        assertEquals(user.getId(), savedUser.getId());
        assertEquals(0, savedUser.getRefreshTokenVersion());

        Optional<User> foundByEmail = userRepositoryAdapter.findByEmail(email);
        assertTrue(foundByEmail.isPresent());
        assertEquals(username, foundByEmail.get().getUsername());
        assertEquals(0, foundByEmail.get().getRefreshTokenVersion());

        Optional<User> foundByUsername = userRepositoryAdapter.findByUsername(username);
        assertTrue(foundByUsername.isPresent());

        assertTrue(userRepositoryAdapter.existsByEmail(email));
        assertTrue(userRepositoryAdapter.existsByUsername(username));
    }
}
