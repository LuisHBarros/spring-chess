package com.chess.auth.domain.service;

import com.chess.auth.domain.exception.InvalidTokenException;
import com.chess.auth.domain.exception.UserNotFoundException;
import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.Password;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.port.EmailService;
import com.chess.auth.domain.port.PasswordEncoder;
import com.chess.auth.domain.port.PasswordRecoveryTokenService;
import com.chess.auth.domain.repository.UserRepository;

public class PasswordRecoveryService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordRecoveryTokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public PasswordRecoveryService(UserRepository userRepository,
                                   EmailService emailService,
                                   PasswordRecoveryTokenService tokenService,
                                   PasswordEncoder passwordEncoder) {
        if (userRepository == null) {
            throw new IllegalArgumentException("UserRepository cannot be null");
        }
        if (emailService == null) {
            throw new IllegalArgumentException("EmailService cannot be null");
        }
        if (tokenService == null) {
            throw new IllegalArgumentException("PasswordRecoveryTokenService cannot be null");
        }
        if (passwordEncoder == null) {
            throw new IllegalArgumentException("PasswordEncoder cannot be null");
        }
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public void initiatePasswordRecovery(Email email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email.getValue()));

        String recoveryToken = tokenService.generateToken(user.getEmail());
        emailService.sendPasswordRecoveryEmail(user.getEmail(), recoveryToken);
    }

    public void resetPassword(Email email, String token, Password newRawPassword) {
        if (!tokenService.validateToken(token, email)) {
            throw new InvalidTokenException("Invalid or expired password recovery token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email.getValue()));

        Password newHashedPassword = passwordEncoder.encode(newRawPassword);
        user.changePassword(newHashedPassword);

        tokenService.invalidateToken(token);
        userRepository.save(user);
    }
}
