# Game Microservice (`game`)

A microservice for Chess Game Logic, Board State, Move Validation, Clocks, and Game History built with **Spring Boot 3** using **Domain-Driven Design (DDD)** architecture and **AWS DynamoDB** + **PostgreSQL**.

---

## 🌟 Features

- **Domain-Driven Design (DDD)**: Clean architecture isolating pure Domain models (`Game`, `Board`, `Piece`, `Move`, `Position`), Value Objects, Domain Services, and Repository Ports from Infrastructure adapters.
- **Infrastructure Layer**:
  - **Spring Data JPA & Hibernate**: Relational persistence for active game states (`GameJpaEntity`) on PostgreSQL (`game_db`).
  - **AWS DynamoDB**: NoSQL persistence for game move histories (`game-history` table with partition key `gameId` and sort key `moveNumber`).
  - **REST Web APIs & OpenAPI**: REST controllers (`GameController`) with Swagger UI documentation at `/swagger-ui.html`.
  - **AWS SNS Messaging**: Asynchronous event publishing to `match-events.fifo` SNS topic with W3C `traceId` context propagation.
  - **AWS SQS Consumer**: Listens to `user-events` queue to handle player profile and system events.
  - **Global Exception Handling**: Centralized exception handling converting domain exceptions to standardized HTTP API responses.
- **Game Aggregate Root**:
  - Encapsulates 8x8 chessboard representation (`Board`), piece rules, turn enforcement, move validation, game statuses (`PENDING`, `IN_PROGRESS`, `CHECK`, `CHECKMATE`, `STALEMATE`, `DRAW`, `RESIGNED`, `TIMEOUT`), and clock state.
  - Invariant rules: Validates turn sequence, piece movement rules, position bounds, and game completion state.
- **Strongly-Typed Value Objects**: Encapsulated validation rules for `GameId` (UUID), `PlayerId` (UUID), `Position` (rank & file), `Piece` (Color & PieceType), `Move` (from/to position, captured piece, promotion), and `GameClock`.
- **Domain Services**:
  - `GameDomainService`: Orchestrates game creation, player turns, move execution, board state updates, game history recording, and match completion events.

---

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.4 (Java 17)
- **AWS & Messaging**: AWS SDK v2 (SNS, SQS, and DynamoDB) via LocalStack
- **Databases**:
  - PostgreSQL (`game_db` / `game_user`) on port `5435`
  - DynamoDB (`game-history` table)
- **Persistence**: Spring Data JPA / Hibernate & AWS DynamoDB Enhanced Client
- **Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Testing**: JUnit 5 (Jupiter), Mockito
- **Containerization**: Docker Compose

---

## 📁 Directory & Package Structure

```
game/
├── docker-compose.yml
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/chess/game/
    │   │   ├── GameApplication.java
    │   │   ├── domain/                         # Pure DDD Domain Layer (Zero Framework Dependencies)
    │   │   │   ├── exception/                  # Domain Exceptions (DomainException, InvalidMoveException, etc.)
    │   │   │   ├── model/                      # Aggregates & Value Objects (Game, Board, Move, Piece, Position, etc.)
    │   │   │   ├── port/                       # Event Publisher Ports (GameEventPublisherPort)
    │   │   │   ├── repository/                 # Repository Ports (GameRepository, GameHistoryRepository)
    │   │   │   └── service/                    # Domain Services (GameDomainService)
    │   │   └── infrastructure/                 # Infrastructure Layer
    │   │       ├── config/                     # Spring Configuration (AwsConfig, DomainServiceConfig, OpenApiConfig)
    │   │       ├── messaging/                  # AWS SNS/SQS Messaging Adapters
    │   │       ├── persistence/                # JPA & DynamoDB Entities, Repositories & Adapters
    │   │       └── web/                        # REST Controllers, DTOs & Global Exception Handler
    │   └── resources/
    │       └── application.yml
    └── test/                                   # Domain & Infrastructure Unit Tests
```

---

## 🚀 Getting Started

### 1. Start Database Infrastructure Container

Spin up the dedicated PostgreSQL database container for the Game service:

```bash
docker compose -f game/docker-compose.yml up -d
```

| Service | Port | Database | Credentials |
|---|---|---|---|
| **PostgreSQL (Game)** | `5435` | `game_db` | User: `game_user` / Pass: `game_pass` |

### 2. Run Tests

To execute the complete unit and integration test suite:

```bash
cd game
mvn clean test
```

### 3. Run the Application

```bash
cd game
mvn spring-boot:run
```

Swagger UI will be accessible at: `http://localhost:8083/swagger-ui.html`.
