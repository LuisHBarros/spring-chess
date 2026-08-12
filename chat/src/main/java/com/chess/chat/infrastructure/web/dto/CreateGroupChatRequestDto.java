package com.chess.chat.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class CreateGroupChatRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "CreatorId is required")
    private UUID creatorId;

    private List<UUID> memberIds;

    public CreateGroupChatRequestDto() {
    }

    public CreateGroupChatRequestDto(String title, UUID creatorId, List<UUID> memberIds) {
        this.title = title;
        this.creatorId = creatorId;
        this.memberIds = memberIds;
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

    public List<UUID> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<UUID> memberIds) {
        this.memberIds = memberIds;
    }
}
