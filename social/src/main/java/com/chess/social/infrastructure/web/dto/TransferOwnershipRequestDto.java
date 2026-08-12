package com.chess.social.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class TransferOwnershipRequestDto {

    @NotNull(message = "ActorId cannot be null")
    private UUID actorId;

    @NotNull(message = "NewOwnerId cannot be null")
    private UUID newOwnerId;

    public TransferOwnershipRequestDto() {
    }

    public TransferOwnershipRequestDto(UUID actorId, UUID newOwnerId) {
        this.actorId = actorId;
        this.newOwnerId = newOwnerId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public UUID getNewOwnerId() {
        return newOwnerId;
    }

    public void setNewOwnerId(UUID newOwnerId) {
        this.newOwnerId = newOwnerId;
    }
}
