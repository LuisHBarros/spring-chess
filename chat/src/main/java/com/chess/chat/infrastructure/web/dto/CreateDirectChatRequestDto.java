package com.chess.chat.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CreateDirectChatRequestDto {

    @NotNull(message = "CreatorId is required")
    private UUID creatorId;

    @NotNull(message = "AddresseeId is required")
    private UUID addresseeId;

    public CreateDirectChatRequestDto() {
    }

    public CreateDirectChatRequestDto(UUID creatorId, UUID addresseeId) {
        this.creatorId = creatorId;
        this.addresseeId = addresseeId;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }

    public UUID getAddresseeId() {
        return addresseeId;
    }

    public void setAddresseeId(UUID addresseeId) {
        this.addresseeId = addresseeId;
    }
}
