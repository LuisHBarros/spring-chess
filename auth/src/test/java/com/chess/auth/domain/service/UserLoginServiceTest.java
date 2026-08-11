package com.chess.auth.domain.service;

import com.chess.auth.domain.exception.InvalidCredentialsException;
import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.Password;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.model.Username;
import com.chess.auth.domain.port.PasswordEncoder;
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
class UserLoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserLoginService loginService;

    private User existingUser;
    private Username username;
    private Email email;
    private Password hashedPassword;
    private Password rawPassword;

    @BeforeEach
    void setUp() {
        loginService = new UserLoginService(userRepository, passwordEncoder);

        username = new Username("player_one");
        email = new Email("player1@chess.com");
        hashedPassword = Password.fromHash("hashedPassword123");
        rawPassword = Password.fromRaw("myPassword123");
        existingUser = User.create(username, email, hashedPassword);
    }

    @Test
    @DisplayName("Should successfully login with email and valid password")
    void shouldSuccessfullyLoginWithEmail() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User loggedInUser = loginService.loginWithEmail(email, rawPassword);

        assertNotNull(loggedInUser);
        assertEquals(email, loggedInUser.getEmail());
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Should successfully login with username and valid password")
    void shouldSuccessfullyLoginWithUsername() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User loggedInUser = loginService.loginWithUsername(username, rawPassword);

        assertNotNull(loggedInUser);
        assertEquals(username, loggedInUser.getUsername());
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when email not found")
    void shouldThrowExceptionWhenEmailNotFound() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () ->
                loginService.loginWithEmail(email, rawPassword)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when password does not match")
    void shouldThrowExceptionWhenPasswordMismatch() {
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () ->
                loginService.loginWithEmail(email, rawPassword)
        );

        verify(userRepository, never()).save(any(User.class));
    }
}
