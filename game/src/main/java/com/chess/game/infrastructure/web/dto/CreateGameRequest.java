package com.chess.game.infrastructure.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateGameRequest(
        @NotNull UUID whitePlayerId,
        @NotNull UUID blackPlayerId,
        @Min(0) int initialTimeSeconds,
        @Min(0) int incrementSeconds
) {}
