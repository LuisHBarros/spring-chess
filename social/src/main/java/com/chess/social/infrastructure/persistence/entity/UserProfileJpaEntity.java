package com.chess.social.infrastructure.persistence.entity;

import com.chess.social.domain.model.Avatar;
import com.chess.social.domain.model.UserId;
import com.chess.social.domain.model.UserProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfileJpaEntity {

    @Id
    private UUID userId;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "status_message")
    private String statusMessage;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserProfileJpaEntity() {
    }

    public UserProfileJpaEntity(UUID userId, String displayName, String avatarUrl, String statusMessage, Instant updatedAt) {
        this.userId = userId;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.statusMessage = statusMessage;
        this.updatedAt = updatedAt;
    }

    public static UserProfileJpaEntity fromDomain(UserProfile profile) {
        return new UserProfileJpaEntity(
                profile.getUserId().getValue(),
                profile.getDisplayName(),
                profile.getAvatar() != null ? profile.getAvatar().getUrl() : null,
                profile.getStatusMessage(),
                profile.getUpdatedAt()
        );
    }

    public UserProfile toDomain() {
        return UserProfile.reconstitute(
                UserId.from(userId),
                displayName,
                avatarUrl != null ? Avatar.of(avatarUrl) : Avatar.defaultAvatar(),
                statusMessage,
                updatedAt
        );
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
