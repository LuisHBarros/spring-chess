package com.chess.auth.infrastructure.web.controller;

import com.chess.auth.domain.exception.InvalidCredentialsException;
import com.chess.auth.domain.exception.UserAlreadyExistsException;
import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.Password;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.model.Username;
import com.chess.auth.domain.service.PasswordRecoveryService;
import com.chess.auth.domain.service.UserLoginService;
import com.chess.auth.domain.service.UserRegistrationService;
import com.chess.auth.infrastructure.web.dto.*;
import com.chess.auth.infrastructure.web.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserRegistrationService registrationService;

    @Mock
    private UserLoginService loginService;

    @Mock
    private PasswordRecoveryService recoveryService;

    @InjectMocks
    private AuthController authController;

    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testUser = User.create(
                new Username("test_user"),
                new Email("test@chess.com"),
                Password.fromHash("hashed_secret_123")
        );
    }

    @Test
    @DisplayName("POST /register - Should return 201 Created on successful registration")
    void register_ShouldReturn201() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("test_user", "test@chess.com", "password123");
        when(registrationService.register(any(), any(), any())).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("test_user"))
                .andExpect(jsonPath("$.email").value("test@chess.com"));
    }

    @Test
    @DisplayName("POST /register - Should return 409 Conflict when user already exists")
    void register_ShouldReturn409WhenUserExists() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("existing_user", "existing@chess.com", "password123");
        when(registrationService.register(any(), any(), any()))
                .thenThrow(new UserAlreadyExistsException("Email address is already in use"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email address is already in use"));
    }

    @Test
    @DisplayName("POST /login - Should return 200 OK with email login")
    void login_ShouldReturn200WithEmail() throws Exception {
        LoginRequestDto dto = new LoginRequestDto("test@chess.com", null, "password123");
        when(loginService.loginWithEmail(any(), any())).thenReturn(testUser);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test_user"));
    }

    @Test
    @DisplayName("POST /login - Should return 401 Unauthorized for invalid credentials")
    void login_ShouldReturn401WhenInvalidCredentials() throws Exception {
        LoginRequestDto dto = new LoginRequestDto("test@chess.com", null, "wrongPass123");
        when(loginService.loginWithEmail(any(), any()))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /recover-password - Should return 200 OK")
    void recoverPassword_ShouldReturn200() throws Exception {
        PasswordRecoveryRequestDto dto = new PasswordRecoveryRequestDto("test@chess.com");

        mockMvc.perform(post("/api/v1/auth/recover-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(recoveryService).initiatePasswordRecovery(any());
    }

    @Test
    @DisplayName("POST /reset-password - Should return 200 OK")
    void resetPassword_ShouldReturn200() throws Exception {
        PasswordResetRequestDto dto = new PasswordResetRequestDto("test@chess.com", "valid-token", "newSecretPass123");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(recoveryService).resetPassword(any(), eq("valid-token"), any());
    }
}
