package com.chess.auth.infrastructure.web.controller;

import com.chess.auth.domain.model.AuthToken;
import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.Password;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.model.Username;
import com.chess.auth.domain.service.PasswordRecoveryService;
import com.chess.auth.domain.service.TokenAuthenticationService;
import com.chess.auth.domain.service.UserLoginService;
import com.chess.auth.domain.service.UserRegistrationService;
import com.chess.auth.infrastructure.security.RateLimiterService;
import com.chess.auth.infrastructure.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRegistrationService registrationService;
    private final UserLoginService loginService;
    private final PasswordRecoveryService recoveryService;
    private final TokenAuthenticationService tokenAuthService;
    private final RateLimiterService rateLimiterService;

    public AuthController(UserRegistrationService registrationService,
                          UserLoginService loginService,
                          PasswordRecoveryService recoveryService,
                          TokenAuthenticationService tokenAuthService,
                          RateLimiterService rateLimiterService) {
        this.registrationService = registrationService;
        this.loginService = loginService;
        this.recoveryService = recoveryService;
        this.tokenAuthService = tokenAuthService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthTokenResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
        Username username = new Username(dto.getUsername());
        Email email = new Email(dto.getEmail());
        Password rawPassword = Password.fromRaw(dto.getPassword());

        User registered = registrationService.register(username, email, rawPassword);
        AuthToken tokens = tokenAuthService.generateTokens(registered);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthTokenResponseDto.from(tokens, UserResponseDto.fromDomain(registered)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        String rateLimitKey = dto.getEmail() != null ? dto.getEmail() : dto.getUsername();
        rateLimiterService.checkRateLimit(rateLimitKey);

        User loggedIn;
        Password rawPassword = Password.fromRaw(dto.getPassword());

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            loggedIn = loginService.loginWithEmail(new Email(dto.getEmail()), rawPassword);
        } else if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            loggedIn = loginService.loginWithUsername(new Username(dto.getUsername()), rawPassword);
        } else {
            throw new IllegalArgumentException("Either email or username must be provided for login");
        }

        AuthToken tokens = tokenAuthService.generateTokens(loggedIn);
        return ResponseEntity.ok(AuthTokenResponseDto.from(tokens, UserResponseDto.fromDomain(loggedIn)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto dto) {
        AuthToken tokens = tokenAuthService.refreshToken(dto.getRefreshToken());
        return ResponseEntity.ok(AuthTokenResponseDto.from(tokens, null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token is required");
        }

        String token = authHeader.substring(7);
        tokenAuthService.logout(token);

        return ResponseEntity.ok(ApiResponseDto.ok("Logged out successfully"));
    }

    @PostMapping("/recover-password")
    public ResponseEntity<ApiResponseDto> initiateRecovery(@Valid @RequestBody PasswordRecoveryRequestDto dto) {
        Email email = new Email(dto.getEmail());
        recoveryService.initiatePasswordRecovery(email);
        return ResponseEntity.ok(ApiResponseDto.ok("Password recovery instructions sent to email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDto> resetPassword(@Valid @RequestBody PasswordResetRequestDto dto) {
        Email email = new Email(dto.getEmail());
        Password newRawPassword = Password.fromRaw(dto.getNewPassword());
        recoveryService.resetPassword(email, dto.getToken(), newRawPassword);
        return ResponseEntity.ok(ApiResponseDto.ok("Password reset successfully"));
    }
}
