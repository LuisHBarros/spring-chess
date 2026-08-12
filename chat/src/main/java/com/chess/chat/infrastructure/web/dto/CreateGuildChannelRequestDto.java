package com.chess.chat.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CreateGuildChannelRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "CreatorId is required")
    private UUID creatorId;

    @NotBlank(message = "GuildId is required")
    private String guildId;

    public CreateGuildChannelRequestDto() {
    }

    public CreateGuildChannelRequestDto(String title, UUID creatorId, String guildId) {
        this.title = title;
        this.creatorId = creatorId;
        this.guildId = guildId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }
}
