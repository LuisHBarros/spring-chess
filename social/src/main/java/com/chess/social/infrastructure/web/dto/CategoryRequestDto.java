package com.chess.social.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CategoryRequestDto {

    @NotNull(message = "ActorId cannot be null")
    private UUID actorId;

    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 50, message = "Category name cannot exceed 50 characters")
    private String name;

    private String description;

    public CategoryRequestDto() {
    }

    public CategoryRequestDto(UUID actorId, String name, String description) {
        this.actorId = actorId;
        this.name = name;
        this.description = description;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
