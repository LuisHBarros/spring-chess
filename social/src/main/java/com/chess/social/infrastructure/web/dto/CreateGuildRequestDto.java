package com.chess.social.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CreateGuildRequestDto {

    @NotBlank(message = "Guild name cannot be blank")
    @Size(min = 3, max = 50, message = "Guild name must be between 3 and 50 characters")
    private String name;

    private String description;

    @NotNull(message = "CreatorId cannot be null")
    private UUID creatorId;

    private String avatarUrl;

    public CreateGuildRequestDto() {
    }

    public CreateGuildRequestDto(String name, String description, UUID creatorId, String avatarUrl) {
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
        this.avatarUrl = avatarUrl;
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

    public UUID getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
