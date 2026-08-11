package com.chess.auth.infrastructure.web.controller;

import com.chess.auth.domain.exception.InvalidCredentialsException;
import com.chess.auth.domain.exception.RateLimitExceededException;
import com.chess.auth.domain.exception.UserAlreadyExistsException;
import com.chess.auth.domain.model.*;
import com.chess.auth.domain.service.PasswordRecoveryService;
import com.chess.auth.domain.service.TokenAuthenticationService;
import com.chess.auth.domain.service.UserLoginService;
import com.chess.auth.domain.service.UserRegistrationService;
import com.chess.auth.infrastructure.security.RateLimiterService;
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

    @Mock
    private TokenAuthenticationService tokenAuthService;

    @Mock
    private RateLimiterService rateLimiterService;

    @InjectMocks
    private AuthController authController;

    private User testUser;
    private AuthToken testAuthToken;

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

        testAuthToken = new AuthToken("access-token-jwt", "refresh-token-jwt", "Bearer", 3600);
    }

    @Test
    @DisplayName("POST /register - Should return 201 Created with JWT tokens")
    void register_ShouldReturn201WithTokens() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("test_user", "test@chess.com", "password123");
        when(registrationService.register(any(), any(), any())).thenReturn(testUser);
        when(tokenAuthService.generateTokens(testUser)).thenReturn(testAuthToken);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token-jwt"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-jwt"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("test_user"));
    }

    @Test
    @DisplayName("POST /register - Should return 400 Bad Request on validation failure")
    void register_ShouldReturn400OnValidationFailure() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("a", "invalid-email", "short");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("POST /login - Should return 200 OK with JWT tokens")
    void login_ShouldReturn200WithTokens() throws Exception {
        LoginRequestDto dto = new LoginRequestDto("test@chess.com", null, "password123");
        when(loginService.loginWithEmail(any(), any())).thenReturn(testUser);
        when(tokenAuthService.generateTokens(testUser)).thenReturn(testAuthToken);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token-jwt"));
    }

    @Test
    @DisplayName("POST /login - Should return 429 Too Many Requests when rate limit exceeded")
    void login_ShouldReturn429WhenRateLimited() throws Exception {
        LoginRequestDto dto = new LoginRequestDto("test@chess.com", null, "password123");
        doThrow(new RateLimitExceededException("Too many login attempts. Please try again in 1 minute."))
                .when(rateLimiterService).checkRateLimit(any());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Too many login attempts. Please try again in 1 minute."));
    }

    @Test
    @DisplayName("POST /login - Should return 401 Unauthorized with generic message on login failure")
    void login_ShouldReturn401WithGenericMessage() throws Exception {
        LoginRequestDto dto = new LoginRequestDto("test@chess.com", null, "wrongPass123");
        when(loginService.loginWithEmail(any(), any()))
                .thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("POST /logout - Should blacklist access token from Authorization header")
    void logout_ShouldBlacklistTokenFromHeader() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer access-token-jwt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(tokenAuthService).logout("access-token-jwt");
    }
}
