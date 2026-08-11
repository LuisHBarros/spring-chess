package com.chess.auth.infrastructure.web.dto;

import com.chess.auth.domain.model.User;

import java.time.Instant;
import java.util.UUID;

public class UserResponseDto {
    private UUID id;
    private String username;
    private String email;
    private Instant createdAt;
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
