package com.chess.auth.domain.model;

import java.time.Instant;
import java.util.Objects;

public class User {
    private final UserId id;
    private Username username;
    private Email email;
    private Password password;
    private final Instant createdAt;
    private Instant lastSeenAt;
    private int refreshTokenVersion;

    private User(UserId id, Username username, Email email, Password password, Instant createdAt, Instant lastSeenAt, int refreshTokenVersion) {
        if (id == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt timestamp cannot be null");
        }
        if (lastSeenAt == null) {
            throw new IllegalArgumentException("lastSeenAt timestamp cannot be null");
        }

        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.lastSeenAt = lastSeenAt;
        this.refreshTokenVersion = refreshTokenVersion;
    }

    public static User create(Username username, Email email, Password hashedPassword) {
        Instant now = Instant.now();
        return new User(UserId.generate(), username, email, hashedPassword, now, now, 0);
    }

    public static User reconstitute(UserId id, Username username, Email email, Password password, Instant createdAt, Instant lastSeenAt) {
        return reconstitute(id, username, email, password, createdAt, lastSeenAt, 0);
    }

    public static User reconstitute(UserId id, Username username, Email email, Password password, Instant createdAt, Instant lastSeenAt, int refreshTokenVersion) {
        return new User(id, username, email, password, createdAt, lastSeenAt, refreshTokenVersion);
    }

    public void updateLastSeenAt(Instant timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        this.lastSeenAt = timestamp;
    }

    public void changePassword(Password newHashedPassword) {
        if (newHashedPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        this.password = newHashedPassword;
        this.refreshTokenVersion++;
    }

    public void incrementRefreshTokenVersion() {
        this.refreshTokenVersion++;
    }

    public int getRefreshTokenVersion() {
        return refreshTokenVersion;
    }

    public UserId getId() {
        return id;
    }

    public Username getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
