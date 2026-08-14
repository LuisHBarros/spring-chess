# Game Microservice (`game`)

A microservice for **Chess Game Logic**, **Board State**, **Move Validation**, **Clocks**, and **Game History** built with **Spring Boot 3** using a **Domain-Driven Design (DDD)** architecture.

It persists active games in **PostgreSQL** and the per-move game history in **AWS DynamoDB**, with events flowing through **AWS SNS/SQS** (LocalStack) on `localhost:4566`.

---

## Domain Model

The `Game` aggregate root lives in `com.chess.game.domain.model` and is the only entry point for mutating a chess game:

- **`Game`** (`Game.java`)
  - Holds the two player IDs, the current `Board`, the current turn color, the `GameStatus`, an optional `GameResult`, move history, the half-move clock, `enPassantTarget`, `GameClock`, creation/update timestamps, and an optimistic-lock `version`.
  - Enforces invariants: turn order, piece movement rules, bounds, check/checkmate/stalemate detection, the 50-move rule, and game completion state.
- **`Board`** (`Board.java`)
  - 8×8 immutable-style map of `Position` → `Piece`, supports `movePiece(Move)`, en passant capture, castling, and pawn promotion.
- **Value Objects**: `GameId`, `PlayerId`, `Position`, `Piece`, `PieceType`, `Move`, `MoveType`, `GameClock`, `GameStatus`, `GameResult`.
- **Domain Service**: `GameDomainService` (`GameDomainService.java`) orchestrates game creation, start, moves, resignation, and event publishing.

### Castling Safety Checks

Castling is only allowed when all of these conditions are met (see `Game.isPseudoLegalMove(...)`, `Game.java` lines 263–275):

1. The king has not moved.
2. The involved rook has not moved.
3. The king is **not** currently in check.
4. The path between the king and rook is clear.
5. The king does not pass through or finish on a square attacked by an opponent piece.

After a move is applied to a test board, `Game.makeMove` verifies the moving player's own king is not left in check before committing the board.

### Game Status Transitions & Clock Tracking

`GameStatus` values (`GameStatus.java`):

- `WAITING_FOR_OPPONENT`
- `IN_PROGRESS`
- `CHECK`
- `CHECKMATE`
- `STALEMATE`
- `DRAW_BY_FIFTY_MOVE_RULE`
- `RESIGNED`
- `TIMEOUT`
- `ABANDONED`
- `DRAW_BY_AGREEMENT`
- `DRAW_BY_REPETITION`
- `DRAW_BY_INSUFFICIENT_MATERIAL`

Flow:

1. `Game.create(...)` starts in `WAITING_FOR_OPPONENT`.
2. `startGame(...)`/`Game.start()` moves it to `IN_PROGRESS`.
3. Each move updates the clock via `GameClock.addIncrement(...)`, then evaluates the next turn for `CHECK`, `CHECKMATE`, `STALEMATE`, or `DRAW_BY_FIFTY_MOVE_RULE`.
4. `resignGame(...)`/`Game.resign(...)` sets `RESIGNED` and a `GameResult`.

> **Clock note:** `GameClock` tracks `whiteTimeRemainingMs` and `blackTimeRemainingMs` and supports `isTimeUp(...)`, but **automatic timeout detection is not fully implemented** — the time state is tracked and stored, yet the service does not automatically end games on flag fall.

---

## Persistence

### PostgreSQL — Active Game State

The `GameJpaEntity` (`infrastructure/persistence/entity/GameJpaEntity.java`) maps to the `games` table and is converted to/from the domain `Game` by `GameStateSerializer`:

- `board_json` (column type `TEXT`) — JSON snapshot of the current `Board` pieces.
- `moves_json` (column type `TEXT`) — JSON array of the move history.
- Clock fields: `white_time_remaining_ms`, `black_time_remaining_ms`, `initial_time_seconds`, `increment_seconds`.
- Metadata: `id`, `white_player_id`, `black_player_id`, `status`, `result`, `current_turn`, `move_count`, `half_move_clock`, `en_passant_target`, `created_at`, `updated_at`.

```java
@Version
@Column(name = "version")
private Long version;
```

The `@Version` column enables **optimistic locking**. Concurrent updates to the same game (e.g., two move attempts) result in `OptimisticLockingFailureException`, which the global handler converts to an HTTP `409 Conflict`.

### DynamoDB — Game History

Every move is written to the **`game-history`** DynamoDB table (`DynamoDbGameHistoryRepositoryAdapter.java`):

- **Partition key:** `gameId` (`String`)
- **Sort key:** `moveNumber` (`Number`)
- **Billing mode:** `PAY_PER_REQUEST`
- **Stored attributes:** `playerId`, `from`, `to`, `pieceType`, `moveType`, `algebraic`, `boardState`, `timestamp`

The table is created by `localstack/init-aws.sh` and also auto-created at startup by `DynamoDbTableInitializer` if it does not already exist.

---

## Domain Exceptions

All domain exceptions extend `DomainException` and are mapped to HTTP responses in `GlobalExceptionHandler` (`infrastructure/web/GlobalExceptionHandler.java`):

| Exception | HTTP Status | Typical Cause |
|---|---|---|
| `InvalidMoveException` | `400 Bad Request` | Illegal piece move, move leaves king in check, promotion missing, etc. |
| `NotPlayerTurnException` | `400 Bad Request` | Player tries to move on the opponent's turn. |
| `PlayerNotFoundException` | `400 Bad Request` | Supplied player ID is not part of the game. |
| `GameAlreadyFinishedException` | `409 Conflict` | Move/start attempted on a game that has already ended. |
| `GameNotFoundException` | `404 Not Found` | Game ID does not exist in the repository. |

---

## REST API & Swagger

`GameController` (`infrastructure/web/controller/GameController.java`) exposes the following endpoints under `/api/v1/games`:

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/games` | Create a new game. |
| `GET` | `/api/v1/games/{gameId}` | Get the current game state. |
| `POST` | `/api/v1/games/{gameId}/start` | Start the game. |
| `POST` | `/api/v1/games/{gameId}/moves` | Make a move. |
| `GET` | `/api/v1/games/{gameId}/history` | Get the move history from DynamoDB. |
| `POST` | `/api/v1/games/{gameId}/resign` | Resign from the game. |
| `GET` | `/api/v1/games/player/{playerId}` | List all games for a player. |

The service runs on **port `8083`** and exposes **Swagger UI** at:

```
http://localhost:8083/swagger-ui/index.html
```

(OpenAPI is configured in `OpenApiConfig.java`.)

---

## Messaging

- **SNS publishing** — `AwsSnsGameEventPublisher` (`infrastructure/messaging/AwsSnsGameEventPublisher.java`) publishes `GAME_CREATED`, `GAME_STARTED`, `MOVE_MADE`, and `GAME_ENDED` events to a **standard SNS topic** named `game-events` (non-FIFO, configured by `aws.sns.game-events-topic-arn`).
- **SQS consuming** — `SqsGameUserEventListener` (`infrastructure/messaging/SqsGameUserEventListener.java`) polls the `game-user-events-queue` (sourced from the `user-events` SNS topic) to react to player/system events.

---

## Configuration

The main settings are in `src/main/resources/application.yml`:

```yaml
server:
  port: 8083

spring:
  config:
    import: optional:file:../.env[.properties]
  datasource:
    url: jdbc:postgresql://localhost:5432/game_db
    username: game_user
    password: ${GAME_DB_PASSWORD}

aws:
  endpoint: ${AWS_ENDPOINT:http://localhost:4566}
  region: ${AWS_REGION:us-east-1}
  sns:
    game-events-topic-arn: ${AWS_GAME_EVENTS_TOPIC_ARN:arn:aws:sns:us-east-1:000000000000:game-events}
  sqs:
    game-user-events-queue-url: ${AWS_GAME_USER_EVENTS_QUEUE_URL:http://localhost:4566/000000000000/game-user-events-queue}
  dynamodb:
    game-history-table-name: ${AWS_GAME_HISTORY_TABLE_NAME:game-history}
```

### `.env` Setup

Create a file named `.env` in the **repository root** (sibling of `game/`, `auth/`, etc.) with at least:

```properties
GAME_DB_PASSWORD=game_pass
```

This value is used by both the Spring DataSource and the LocalStack RDS init script (`localstack/init-aws.sh`), which fall back to `game_pass` when the variable is not present.

---

## Local Development

### 1. Start LocalStack & Game Database

`game/docker-compose.yml` starts a LocalStack container with the following ports:

| Port Mapping | Purpose |
|---|---|
| `4566:4566` | LocalStack edge service (SNS, SQS, S3, DynamoDB, RDS) |
| `4510-4559:4510-4559` | Additional LocalStack service ports |
| `5435:5432` | **Game PostgreSQL port** — `localhost:5435` on the host maps to port `5432` inside the container |

Run:

```bash
docker compose -f game/docker-compose.yml up -d
```

The `env_file` points to `../.env`, and the init script creates the `game_db` RDS instance and the `game-history` DynamoDB table.

### 2. Run the Application

```bash
cd game
mvn spring-boot:run
```

### 3. Open Swagger

```
http://localhost:8083/swagger-ui/index.html
```

---

## Testing & CI

- Unit tests exist in `game/src/test` (domain models, `GameDomainService`, `GameStateSerializer`, `GlobalExceptionHandler`, `GameControllerEndpointsTest`).
- **CI does not currently test `game`**. The repository workflows (`.github/workflows/ci.yml` and `.github/workflows/build.yml`) only run `mvn verify` for the `auth` and `social` services.
