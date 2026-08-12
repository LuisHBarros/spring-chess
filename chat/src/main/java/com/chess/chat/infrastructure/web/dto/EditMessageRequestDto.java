package com.chess.chat.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class EditMessageRequestDto {

    @NotNull(message = "ActorId is required")
    private UUID actorId;

    @NotBlank(message = "New content is required")
    private String newContent;

    public EditMessageRequestDto() {
    }

    public EditMessageRequestDto(UUID actorId, String newContent) {
        this.actorId = actorId;
        this.newContent = newContent;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getNewContent() {
        return newContent;
    }

    public void setNewContent(String newContent) {
        this.newContent = newContent;
    }
}
