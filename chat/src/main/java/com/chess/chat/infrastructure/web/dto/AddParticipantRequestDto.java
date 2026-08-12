package com.chess.chat.infrastructure.web.dto;

import com.chess.chat.domain.model.ParticipantRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AddParticipantRequestDto {

    @NotNull(message = "ActorId is required")
    private UUID actorId;

    @NotNull(message = "NewParticipantId is required")
    private UUID newParticipantId;

    private ParticipantRole role = ParticipantRole.MEMBER;

    public AddParticipantRequestDto() {
    }

    public AddParticipantRequestDto(UUID actorId, UUID newParticipantId, ParticipantRole role) {
        this.actorId = actorId;
        this.newParticipantId = newParticipantId;
        this.role = role != null ? role : ParticipantRole.MEMBER;
    }

    public UUID getActorId() {
        return actorId;
    }

    public void setActorId(UUID actorId) {
        this.actorId = actorId;
    }

    public UUID getNewParticipantId() {
        return newParticipantId;
    }

    public void setNewParticipantId(UUID newParticipantId) {
        this.newParticipantId = newParticipantId;
    }

    public ParticipantRole getRole() {
        return role;
    }

    public void setRole(ParticipantRole role) {
        this.role = role;
    }
}
