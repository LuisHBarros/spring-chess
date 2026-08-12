package com.chess.chat.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CreateMatchChatRequestDto {

    @NotNull(message = "Player1Id is required")
    private UUID player1Id;

    @NotNull(message = "Player2Id is required")
    private UUID player2Id;

    @NotBlank(message = "MatchId is required")
    private String matchId;

    public CreateMatchChatRequestDto() {
    }

    public CreateMatchChatRequestDto(UUID player1Id, UUID player2Id, String matchId) {
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.matchId = matchId;
    }

    public UUID getPlayer1Id() {
        return player1Id;
    }

    public void setPlayer1Id(UUID player1Id) {
        this.player1Id = player1Id;
    }

    public UUID getPlayer2Id() {
        return player2Id;
    }

    public void setPlayer2Id(UUID player2Id) {
        this.player2Id = player2Id;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }
}
