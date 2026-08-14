package com.chess.auth.domain.service;

import com.chess.auth.domain.exception.UserAlreadyExistsException;
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
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserRegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new UserRegistrationService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void shouldSuccessfullyRegisterUser() {
        Username username = new Username("new_player");
        Email email = new Email("player@chess.com");
        Password rawPassword = Password.fromRaw("SecurePass123!");
        Password hashedPassword = Password.fromHash("hashed_SecurePass123!");

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = registrationService.register(username, email, rawPassword);

        assertNotNull(registeredUser);
        assertEquals(username, registeredUser.getUsername());
        assertEquals(email, registeredUser.getEmail());
        assertEquals(hashedPassword, registeredUser.getPassword());

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException if email already registered")
    void shouldThrowExceptionWhenEmailExists() {
        Username username = new Username("new_player");
        Email email = new Email("existing@chess.com");
        Password rawPassword = Password.fromRaw("SecurePass123!");

        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () ->
                registrationService.register(username, email, rawPassword)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException if username already taken")
    void shouldThrowExceptionWhenUsernameTaken() {
        Username username = new Username("existing_user");
        Email email = new Email("new@chess.com");
        Password rawPassword = Password.fromRaw("SecurePass123!");

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByUsername(username)).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () ->
                registrationService.register(username, email, rawPassword)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException on unique constraint violation")
    void shouldThrowUserAlreadyExistsOnUniqueConstraintViolation() {
        Username username = new Username("racing_user");
        Email email = new Email("race@chess.com");
        Password rawPassword = Password.fromRaw("SecurePass123!");
        Password hashedPassword = Password.fromHash("hashed_race");

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("unique constraint"));

        assertThrows(UserAlreadyExistsException.class, () ->
                registrationService.register(username, email, rawPassword)
        );
    }
}
