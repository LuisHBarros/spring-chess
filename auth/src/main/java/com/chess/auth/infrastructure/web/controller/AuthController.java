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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and account management endpoints")
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

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns JWT access and refresh tokens. "
                    + "Username must be 3-30 alphanumeric characters (underscores allowed). "
                    + "Password must be at least 8 characters."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthTokenResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (validation errors)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"Username must be between 3 and 30 characters\"}"))),
            @ApiResponse(responseCode = "409", description = "User already exists (duplicate email or username)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"User with this email already exists\"}")))
    })
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

    @Operation(
            summary = "Login",
            description = "Authenticates a user with email or username and password. "
                    + "Returns JWT access and refresh tokens. Rate-limited per identity."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthTokenResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Missing credentials or invalid input",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"Either email or username must be provided for login\"}"))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"Invalid email or password\"}"))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"User not found\"}"))),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"Too many login attempts. Please try again later\"}")))
    })
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

    @Operation(
            summary = "Refresh access token",
            description = "Exchanges a valid refresh token for a new pair of access and refresh tokens. "
                    + "The user field in the response will be null."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthTokenResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"Invalid or expired refresh token\"}")))
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto dto) {
        AuthToken tokens = tokenAuthService.refreshToken(dto.getRefreshToken());
        return ResponseEntity.ok(AuthTokenResponseDto.from(tokens, null));
    }

    @Operation(
            summary = "Logout",
            description = "Invalidates the provided JWT access token by adding it to a blacklist. "
                    + "Requires the Authorization header with a Bearer token.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"message\": \"Logged out successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "Missing or malformed Authorization header",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"Authorization header with Bearer token is required\"}")))
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto> logout(
            @Parameter(description = "Bearer token (format: 'Bearer {token}')", required = true,
                    example = "Bearer eyJhbGciOiJSUzI1NiJ9...")
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Authorization header with Bearer token is required");
        }

        String token = authHeader.substring(7);
        tokenAuthService.logout(token);

        return ResponseEntity.ok(ApiResponseDto.ok("Logged out successfully"));
    }

    @Operation(
            summary = "Initiate password recovery",
            description = "Sends a password recovery email with a reset token to the provided email address. "
                    + "Always returns 200 OK regardless of whether the email exists, to prevent user enumeration."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recovery instructions sent (if email exists)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"message\": \"Password recovery instructions sent to email\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid email format",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"Invalid email format\"}")))
    })
    @PostMapping("/recover-password")
    public ResponseEntity<ApiResponseDto> initiateRecovery(@Valid @RequestBody PasswordRecoveryRequestDto dto) {
        Email email = new Email(dto.getEmail());
        recoveryService.initiatePasswordRecovery(email);
        return ResponseEntity.ok(ApiResponseDto.ok("Password recovery instructions sent to email"));
    }

    @Operation(
            summary = "Reset password",
            description = "Resets the user's password using a valid recovery token received via email. "
                    + "The new password must be at least 8 characters."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": true, \"message\": \"Password reset successfully\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid token, email, or password",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = "{\"success\": false, \"message\": \"Invalid or expired recovery token\"}")))
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDto> resetPassword(@Valid @RequestBody PasswordResetRequestDto dto) {
        Email email = new Email(dto.getEmail());
        Password newRawPassword = Password.fromRaw(dto.getNewPassword());
        recoveryService.resetPassword(email, dto.getToken(), newRawPassword);
        return ResponseEntity.ok(ApiResponseDto.ok("Password reset successfully"));
    }
}
