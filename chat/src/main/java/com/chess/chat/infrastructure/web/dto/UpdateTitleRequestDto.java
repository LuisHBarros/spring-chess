package com.chess.chat.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class UpdateTitleRequestDto {

    @NotNull(message = "ActorId is required")
    private UUID actorId;

    @NotBlank(message = "New title is required")
    private String newTitle;

    public UpdateTitleRequestDto() {
    }

    public UpdateTitleRequestDto(UUID actorId, String newTitle) {
        this.actorId = actorId;
        this.newTitle = newTitle;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getNewTitle() {
        return newTitle;
    }

    public void setNewTitle(String newTitle) {
        this.newTitle = newTitle;
    }
}
