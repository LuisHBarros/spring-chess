package com.chess.auth.domain.service;

import com.chess.auth.domain.exception.InvalidTokenException;
import com.chess.auth.domain.exception.UserNotFoundException;
import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.Password;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.model.Username;
import com.chess.auth.domain.port.EmailService;
import com.chess.auth.domain.port.PasswordEncoder;
import com.chess.auth.domain.port.PasswordRecoveryTokenService;
import com.chess.auth.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordRecoveryTokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordRecoveryService recoveryService;

    private User existingUser;
    private Email email;

    @BeforeEach
    void setUp() {
        recoveryService = new PasswordRecoveryService(userRepository, emailService, tokenService, passwordEncoder);

        email = new Email("user@chess.com");
        existingUser = User.create(
                new Username("chess_user"),
                email,
                Password.fromHash("oldHashedPass")
        );
    }

    @Test
    @DisplayName("Should initiate password recovery by generating token and sending email")
    void shouldInitiatePasswordRecovery() {
        String mockToken = "recovery-token-12345";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(tokenService.generateToken(email)).thenReturn(mockToken);

        recoveryService.initiatePasswordRecovery(email);

        verify(tokenService).generateToken(email);
        verify(emailService).sendPasswordRecoveryEmail(email, mockToken);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when initiating recovery for unregistered email")
    void shouldThrowExceptionWhenUserNotFoundForRecovery() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                recoveryService.initiatePasswordRecovery(email)
        );

        verify(tokenService, never()).generateToken(any());
        verify(emailService, never()).sendPasswordRecoveryEmail(any(), any());
    }

    @Test
    @DisplayName("Should reset password when token is valid")
    void shouldResetPasswordSuccessfully() {
        String token = "valid-token";
        Password newRawPassword = Password.fromRaw("NewSecretPass123!");
        Password newHashedPassword = Password.fromHash("hashed_NewSecretPass123!");

        when(tokenService.validateToken(token, email)).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(newRawPassword)).thenReturn(newHashedPassword);

        recoveryService.resetPassword(email, token, newRawPassword);

        assertEquals(newHashedPassword, existingUser.getPassword());
        verify(tokenService).invalidateToken(token);
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException when token is invalid or expired")
    void shouldThrowExceptionWhenTokenInvalid() {
        String invalidToken = "invalid-token";
        Password newRawPassword = Password.fromRaw("NewSecretPass123!");

        when(tokenService.validateToken(invalidToken, email)).thenReturn(false);

        assertThrows(InvalidTokenException.class, () ->
                recoveryService.resetPassword(email, invalidToken, newRawPassword)
        );

        verify(userRepository, never()).save(any());
    }
}
