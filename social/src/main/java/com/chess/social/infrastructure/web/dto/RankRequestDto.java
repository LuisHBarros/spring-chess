package com.chess.social.infrastructure.web.dto;

import com.chess.social.domain.model.RankPermission;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public class RankRequestDto {

    @NotNull(message = "ActorId cannot be null")
    private UUID actorId;

    @NotBlank(message = "Rank name cannot be blank")
    @Size(max = 50, message = "Rank name cannot exceed 50 characters")
    private String name;

    @Min(value = 1, message = "Priority must be positive")
    private int priority;

    private Set<RankPermission> permissions;

    public RankRequestDto() {
    }

    public RankRequestDto(UUID actorId, String name, int priority, Set<RankPermission> permissions) {
        this.actorId = actorId;
        this.name = name;
        this.priority = priority;
        this.permissions = permissions;
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Set<RankPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<RankPermission> permissions) {
        this.permissions = permissions;
    }
}
