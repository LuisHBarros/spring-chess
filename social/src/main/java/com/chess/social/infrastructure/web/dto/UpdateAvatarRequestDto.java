package com.chess.social.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class UpdateAvatarRequestDto {

    @NotNull(message = "ActorId cannot be null")
    private UUID actorId;

    @NotBlank(message = "Avatar URL cannot be blank")
    private String avatarUrl;

    public UpdateAvatarRequestDto() {
    }

    public UpdateAvatarRequestDto(UUID actorId, String avatarUrl) {
        this.actorId = actorId;
        this.avatarUrl = avatarUrl;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
