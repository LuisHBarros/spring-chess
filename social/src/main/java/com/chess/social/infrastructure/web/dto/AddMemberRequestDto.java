package com.chess.social.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AddMemberRequestDto {

    @NotNull(message = "ActorId cannot be null")
    private UUID actorId;

    @NotNull(message = "NewMemberId cannot be null")
    private UUID newMemberId;

    private UUID initialRankId;

    public AddMemberRequestDto() {
    }

    public AddMemberRequestDto(UUID actorId, UUID newMemberId, UUID initialRankId) {
        this.actorId = actorId;
        this.newMemberId = newMemberId;
        this.initialRankId = initialRankId;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public UUID getNewMemberId() {
        return newMemberId;
    }

    public void setNewMemberId(UUID newMemberId) {
        this.newMemberId = newMemberId;
    }

    public UUID getInitialRankId() {
        return initialRankId;
    }

    public void setInitialRankId(UUID initialRankId) {
        this.initialRankId = initialRankId;
    }
}
