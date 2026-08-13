package com.chess.game.infrastructure.web.controller;

import com.chess.game.domain.model.Game;
import com.chess.game.domain.model.GameId;
import com.chess.game.domain.model.PlayerId;
import com.chess.game.domain.service.GameDomainService;
import com.chess.game.infrastructure.web.GlobalExceptionHandler;
import com.chess.game.infrastructure.web.dto.CreateGameRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GameControllerEndpointsTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GameDomainService gameDomainService;

    @InjectMocks
    private GameController gameController;

    private PlayerId whitePlayer;
    private PlayerId blackPlayer;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(gameController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        whitePlayer = PlayerId.generate();
        blackPlayer = PlayerId.generate();
    }

    @Test
    @DisplayName("Should create game via POST /api/v1/games")
    void shouldCreateGameEndpoint() throws Exception {
        Game game = Game.create(whitePlayer, blackPlayer, 600, 5);
        when(gameDomainService.createGame(any(), any(), eq(600), eq(5))).thenReturn(game);

        CreateGameRequest request = new CreateGameRequest(whitePlayer.getValue(), blackPlayer.getValue(), 600, 5);

        mockMvc.perform(post("/api/v1/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.whitePlayerId").value(whitePlayer.toString()));
    }

    @Test
    @DisplayName("Should fetch game state via GET /api/v1/games/{id}")
    void shouldGetGameEndpoint() throws Exception {
        Game game = Game.create(whitePlayer, blackPlayer, 600, 5);
        when(gameDomainService.getGame(eq(game.getId()))).thenReturn(game);

        mockMvc.perform(get("/api/v1/games/{gameId}", game.getId().getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(game.getId().toString()));
    }
}
