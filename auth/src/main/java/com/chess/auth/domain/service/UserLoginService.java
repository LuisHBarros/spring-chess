package com.chess.auth.domain.service;

import com.chess.auth.domain.exception.InvalidCredentialsException;
import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.Password;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.model.Username;
import com.chess.auth.domain.port.PasswordEncoder;
import com.chess.auth.domain.repository.UserRepository;

import java.time.Instant;

public class UserLoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserLoginService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        if (passwordEncoder == null) {
            throw new IllegalArgumentException("PasswordEncoder cannot be null");
        }
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User loginWithEmail(Email email, Password rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        return authenticateAndTouch(user, rawPassword);
    }

    public User loginWithUsername(Username username, Password rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        return authenticateAndTouch(user, rawPassword);
    }

    private User authenticateAndTouch(User user, Password rawPassword) {
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        user.updateLastSeenAt(Instant.now());
        return userRepository.save(user);
    }
}
