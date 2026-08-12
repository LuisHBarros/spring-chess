package com.chess.social.domain.model;

import java.time.Instant;
import java.util.Objects;

public class UserProfile {
    private final UserId userId;
    private String displayName;
    private Avatar avatar;
    private String statusMessage;
    private Instant updatedAt;

    private UserProfile(UserId userId, String displayName, Avatar avatar, String statusMessage, Instant updatedAt) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be null or empty");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("UpdatedAt timestamp cannot be null");
        }

        this.userId = userId;
        this.displayName = displayName.trim();
        this.avatar = avatar != null ? avatar : Avatar.defaultAvatar();
        this.statusMessage = statusMessage != null ? statusMessage.trim() : "";
        this.updatedAt = updatedAt;
    }

    public static UserProfile create(UserId userId, String displayName, Avatar avatar) {
        return new UserProfile(userId, displayName, avatar, "", Instant.now());
    }

    public static UserProfile reconstitute(UserId userId, String displayName, Avatar avatar, String statusMessage, Instant updatedAt) {
        return new UserProfile(userId, displayName, avatar, statusMessage, updatedAt);
    }

    public void updateAvatar(Avatar newAvatar) {
        if (newAvatar == null) {
            throw new IllegalArgumentException("Avatar cannot be null");
        }
        this.avatar = newAvatar;
        this.updatedAt = Instant.now();
    }

    public void updateProfile(String newDisplayName, String newStatusMessage) {
        if (newDisplayName == null || newDisplayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be null or empty");
        }
        this.displayName = newDisplayName.trim();
        this.statusMessage = newStatusMessage != null ? newStatusMessage.trim() : "";
        this.updatedAt = Instant.now();
    }

    public UserId getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Avatar getAvatar() {
        return avatar;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserProfile profile = (UserProfile) o;
        return Objects.equals(userId, profile.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
