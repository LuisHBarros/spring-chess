package com.chess.game.domain.service;

import com.chess.game.domain.exception.GameNotFoundException;
import com.chess.game.domain.model.Game;
import com.chess.game.domain.model.GameId;
import com.chess.game.domain.model.Move;
import com.chess.game.domain.model.PlayerId;
import com.chess.game.domain.model.Position;
import com.chess.game.domain.port.GameEventPublisherPort;
import com.chess.game.domain.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameDomainServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameEventPublisherPort eventPublisher;

    private GameDomainService gameDomainService;
    private PlayerId whitePlayer;
    private PlayerId blackPlayer;

    @BeforeEach
    void setUp() {
        gameDomainService = new GameDomainService(gameRepository, eventPublisher);
        whitePlayer = PlayerId.generate();
        blackPlayer = PlayerId.generate();
    }

    @Test
    @DisplayName("Should create game and publish GAME_CREATED event")
    void shouldCreateGame() {
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Game created = gameDomainService.createGame(whitePlayer, blackPlayer, 600, 5);

        assertThat(created.getWhitePlayerId()).isEqualTo(whitePlayer);
        assertThat(created.getBlackPlayerId()).isEqualTo(blackPlayer);
        verify(gameRepository).save(any(Game.class));
        verify(eventPublisher).publishGameEvent(eq("GAME_CREATED"), any(), any());
    }

    @Test
    @DisplayName("Should start game and publish GAME_STARTED event")
    void shouldStartGame() {
        Game game = Game.create(whitePlayer, blackPlayer, 600, 5);
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Game started = gameDomainService.startGame(game.getId());

        assertThat(started.getStatus()).isEqualTo(com.chess.game.domain.model.GameStatus.IN_PROGRESS);
        verify(eventPublisher).publishGameEvent(eq("GAME_STARTED"), eq(game.getId().toString()), any());
    }

    @Test
    @DisplayName("Should throw GameNotFoundException when game does not exist")
    void shouldThrowWhenGameNotFound() {
        GameId unknownId = GameId.generate();
        when(gameRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameDomainService.getGame(unknownId))
                .isInstanceOf(GameNotFoundException.class);
    }
}
