package com.chess.game.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MakeMoveRequest(
        @NotNull java.util.UUID playerId,
        @NotBlank String from,
        @NotBlank String to,
        String promotionPiece
) {}
