package com.chess.auth.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.chess.auth.domain.model.User;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "User profile information")
public class UserResponseDto {

    @Schema(description = "Unique user identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Username", example = "chess_master")
    private String username;

    @Schema(description = "User email address", example = "player@chess.com")
    private String email;

    @Schema(description = "Account creation timestamp", example = "2024-01-15T10:30:00Z")
    private Instant createdAt;

    @Schema(description = "Last activity timestamp", example = "2024-06-20T14:22:00Z")
    private Instant lastSeenAt;

    public UserResponseDto() {}

    public UserResponseDto(UUID id, String username, String email, Instant createdAt, Instant lastSeenAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.lastSeenAt = lastSeenAt;
    }

    public static UserResponseDto fromDomain(User user) {
        return new UserResponseDto(
                user.getId().getValue(),
                user.getUsername().getValue(),
                user.getEmail().getValue(),
                user.getCreatedAt(),
                user.getLastSeenAt()
        );
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastSeenAt() { return lastSeenAt; }
}
