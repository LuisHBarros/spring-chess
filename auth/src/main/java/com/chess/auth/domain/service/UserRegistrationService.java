package com.chess.auth.domain.service;

import com.chess.auth.domain.exception.UserAlreadyExistsException;
import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.Password;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.model.Username;
import com.chess.auth.domain.port.PasswordEncoder;
import com.chess.auth.domain.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class UserRegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        if (passwordEncoder == null) {
            throw new IllegalArgumentException("PasswordEncoder cannot be null");
        }
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(Username username, Email email, Password rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email address is already in use: " + email.getValue());
        }
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username is already taken: " + username.getValue());
        }

        Password hashedPassword = passwordEncoder.encode(rawPassword);
        User newUser = User.create(username, email, hashedPassword);
        try {
            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException("Email or username is already in use");
        }
    }
}
