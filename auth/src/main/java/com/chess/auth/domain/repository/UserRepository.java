package com.chess.auth.domain.repository;

import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.model.UserId;
import com.chess.auth.domain.model.Username;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UserId id);
    Optional<User> findByEmail(Email email);
    Optional<User> findByUsername(Username username);
    boolean existsByEmail(Email email);
    boolean existsByUsername(Username username);
}
