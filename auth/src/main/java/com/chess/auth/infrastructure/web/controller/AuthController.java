package com.chess.auth.infrastructure.web.controller;

import com.chess.auth.domain.model.Email;
import com.chess.auth.domain.model.Password;
import com.chess.auth.domain.model.User;
import com.chess.auth.domain.model.Username;
import com.chess.auth.domain.service.PasswordRecoveryService;
import com.chess.auth.domain.service.UserLoginService;
import com.chess.auth.domain.service.UserRegistrationService;
import com.chess.auth.infrastructure.web.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRegistrationService registrationService;
    private final UserLoginService loginService;
    private final PasswordRecoveryService recoveryService;

    public AuthController(UserRegistrationService registrationService,
                          UserLoginService loginService,
                          PasswordRecoveryService recoveryService) {
        this.registrationService = registrationService;
        this.loginService = loginService;
        this.recoveryService = recoveryService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody RegisterRequestDto dto) {
        Username username = new Username(dto.getUsername());
        Email email = new Email(dto.getEmail());
        Password rawPassword = Password.fromRaw(dto.getPassword());

        User registered = registrationService.register(username, email, rawPassword);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDto.fromDomain(registered));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@RequestBody LoginRequestDto dto) {
        User loggedIn;
        Password rawPassword = Password.fromRaw(dto.getPassword());

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            loggedIn = loginService.loginWithEmail(new Email(dto.getEmail()), rawPassword);
        } else if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            loggedIn = loginService.loginWithUsername(new Username(dto.getUsername()), rawPassword);
        } else {
            throw new IllegalArgumentException("Either email or username must be provided for login");
        }

        return ResponseEntity.ok(UserResponseDto.fromDomain(loggedIn));
    }

    @PostMapping("/recover-password")
    public ResponseEntity<ApiResponseDto> initiateRecovery(@RequestBody PasswordRecoveryRequestDto dto) {
        Email email = new Email(dto.getEmail());
        recoveryService.initiatePasswordRecovery(email);
        return ResponseEntity.ok(ApiResponseDto.ok("Password recovery instructions sent to email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDto> resetPassword(@RequestBody PasswordResetRequestDto dto) {
        Email email = new Email(dto.getEmail());
        Password newRawPassword = Password.fromRaw(dto.getNewPassword());
        recoveryService.resetPassword(email, dto.getToken(), newRawPassword);
        return ResponseEntity.ok(ApiResponseDto.ok("Password reset successfully"));
    }
}
