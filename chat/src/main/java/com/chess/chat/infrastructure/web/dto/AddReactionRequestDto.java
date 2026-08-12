package com.chess.chat.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AddReactionRequestDto {

    @NotNull(message = "ActorId is required")
    private UUID actorId;

    @NotBlank(message = "Emoji is required")
    private String emoji;

    public AddReactionRequestDto() {
    }

    public AddReactionRequestDto(UUID actorId, String emoji) {
        this.actorId = actorId;
        this.emoji = emoji;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
}
