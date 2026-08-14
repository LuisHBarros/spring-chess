package com.chess.auth.infrastructure.persistence.entity;

import com.chess.auth.domain.model.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 30)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    @Column(name = "refresh_token_version", nullable = false)
    private int refreshTokenVersion;

    public UserJpaEntity() {
    }

    public UserJpaEntity(UUID id, String username, String email, String password, Instant createdAt, Instant lastSeenAt, int refreshTokenVersion) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.lastSeenAt = lastSeenAt;
        this.refreshTokenVersion = refreshTokenVersion;
    }

    public static UserJpaEntity fromDomain(User user) {
        return new UserJpaEntity(
                user.getId().getValue(),
                user.getUsername().getValue(),
                user.getEmail().getValue(),
                user.getPassword().getValue(),
                user.getCreatedAt(),
                user.getLastSeenAt(),
                user.getRefreshTokenVersion()
        );
    }

    public User toDomain() {
        return User.reconstitute(
                UserId.from(this.id),
                new Username(this.username),
                new Email(this.email),
                Password.fromHash(this.password),
                this.createdAt,
                this.lastSeenAt,
                this.refreshTokenVersion
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public int getRefreshTokenVersion() {
        return refreshTokenVersion;
    }

    public void setRefreshTokenVersion(int refreshTokenVersion) {
        this.refreshTokenVersion = refreshTokenVersion;
    }
}
