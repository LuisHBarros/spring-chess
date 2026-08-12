package com.chess.game.infrastructure.web.controller;

import com.chess.game.domain.model.Game;
import com.chess.game.domain.model.GameHistoryRecord;
import com.chess.game.domain.model.GameId;
import com.chess.game.domain.model.Move;
import com.chess.game.domain.model.PieceType;
import com.chess.game.domain.model.PlayerId;
import com.chess.game.domain.model.Position;
import com.chess.game.domain.service.GameDomainService;
import com.chess.game.infrastructure.web.dto.ApiResponseDto;
import com.chess.game.infrastructure.web.dto.CreateGameRequest;
import com.chess.game.infrastructure.web.dto.GameHistoryResponseDto;
import com.chess.game.infrastructure.web.dto.GameResponseDto;
import com.chess.game.infrastructure.web.dto.MakeMoveRequest;
import com.chess.game.infrastructure.web.dto.MoveResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/games")
@Tag(name = "Games", description = "Endpoints for managing chess games")
public class GameController {

    private final GameDomainService gameDomainService;

    public GameController(GameDomainService gameDomainService) {
        this.gameDomainService = gameDomainService;
    }

    @PostMapping
    @Operation(summary = "Create a new chess game")
    public ResponseEntity<ApiResponseDto<GameResponseDto>> createGame(@Valid @RequestBody CreateGameRequest request) {
        Game game = gameDomainService.createGame(
                PlayerId.from(request.whitePlayerId()),
                PlayerId.from(request.blackPlayerId()),
                request.initialTimeSeconds(),
                request.incrementSeconds()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(GameResponseDto.fromDomain(game)));
    }

    @GetMapping("/{gameId}")
    @Operation(summary = "Get game state by ID")
    public ResponseEntity<ApiResponseDto<GameResponseDto>> getGame(@PathVariable UUID gameId) {
        Game game = gameDomainService.getGame(GameId.from(gameId));
        return ResponseEntity.ok(ApiResponseDto.success(GameResponseDto.fromDomain(game)));
    }

    @PostMapping("/{gameId}/start")
    @Operation(summary = "Start a game")
    public ResponseEntity<ApiResponseDto<GameResponseDto>> startGame(@PathVariable UUID gameId) {
        Game game = gameDomainService.startGame(GameId.from(gameId));
        return ResponseEntity.ok(ApiResponseDto.success(GameResponseDto.fromDomain(game)));
    }

    @PostMapping("/{gameId}/moves")
    @Operation(summary = "Make a move in a game")
    public ResponseEntity<ApiResponseDto<MoveResponseDto>> makeMove(
            @PathVariable UUID gameId, @Valid @RequestBody MakeMoveRequest request) {
        com.chess.game.infrastructure.security.SecurityUtils.validateUserIdentity(request.playerId());
        PieceType promotionPiece = request.promotionPiece() != null
                ? PieceType.valueOf(request.promotionPiece().toUpperCase())
                : null;
        Move move = gameDomainService.makeMove(
                GameId.from(gameId),
                PlayerId.from(request.playerId()),
                Position.fromAlgebraic(request.from()),
                Position.fromAlgebraic(request.to()),
                promotionPiece
        );
        return ResponseEntity.ok(ApiResponseDto.success(MoveResponseDto.fromDomain(move)));
    }

    @GetMapping("/{gameId}/history")
    @Operation(summary = "Get game move history from DynamoDB")
    public ResponseEntity<ApiResponseDto<List<GameHistoryResponseDto>>> getGameHistory(@PathVariable UUID gameId) {
        List<GameHistoryRecord> history = gameDomainService.getGameHistory(GameId.from(gameId));
        List<GameHistoryResponseDto> response = history.stream()
                .map(GameHistoryResponseDto::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }

    @PostMapping("/{gameId}/resign")
    @Operation(summary = "Resign from a game")
    public ResponseEntity<ApiResponseDto<GameResponseDto>> resignGame(
            @PathVariable UUID gameId, @RequestBody UUID playerId) {
        com.chess.game.infrastructure.security.SecurityUtils.validateUserIdentity(playerId);
        Game game = gameDomainService.resignGame(GameId.from(gameId), PlayerId.from(playerId));
        return ResponseEntity.ok(ApiResponseDto.success(GameResponseDto.fromDomain(game)));
    }

    @GetMapping("/player/{playerId}")
    @Operation(summary = "Get all games for a player")
    public ResponseEntity<ApiResponseDto<List<GameResponseDto>>> getPlayerGames(@PathVariable UUID playerId) {
        List<Game> games = gameDomainService.getPlayerGames(PlayerId.from(playerId));
        List<GameResponseDto> response = games.stream()
                .map(GameResponseDto::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponseDto.success(response));
    }
}
