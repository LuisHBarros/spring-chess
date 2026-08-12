# Chat Microservice (`chat`)

A microservice for Real-Time Direct Messaging, Group Chats, Guild Channels, Match Messaging, and Message Moderation built with **Spring Boot 3** using **Domain-Driven Design (DDD)** architecture.

---

## 🌟 Features

- **Domain-Driven Design (DDD)**: Clean architecture isolating pure Domain models, Value Objects, Domain Services, and Repository Ports from Infrastructure adapters.
- **Infrastructure Layer**:
  - **Spring Data JPA & Hibernate**: Database persistence via JPA entities (`ChatRoomJpaEntity`, `MessageJpaEntity`, `ChatParticipantJpaEntity`, `MessageReactionJpaEntity`) with Repository Adapters.
  - **REST Web APIs & OpenAPI**: REST controllers (`ChatRoomController`, `MessageController`) with Swagger UI documentation at `/swagger-ui.html`.
  - **AWS SNS Messaging**: Asynchronous event publishing to `chat-events` SNS topic with W3C `traceId` context propagation.
  - **Global Exception Handling**: Centralized exception handling converting domain exceptions to standardized HTTP API responses.
- **ChatRoom Aggregate Root**:
  - Support for multiple Chat Room types: `DIRECT` (1-on-1 private messaging), `GROUP` (Multi-user group chats), `GUILD` (Guild channels linked to `social` microservice), and `MATCH` (In-game chess match chat).
  - Invariant rules: Prevents adding participants to `DIRECT` rooms, enforces maximum direct chat participant limits, checks role permissions (`OWNER`, `ADMIN`, `MEMBER`), and handles room archiving.
- **Message Aggregate Root**:
  - Rich message types (`TEXT`, `SYSTEM`, `IMAGE`, `GAME_INVITE`, `CHESS_MOVE_ANNOTATION`).
  - Full message lifecycle management (`SENT`, `DELIVERED`, `READ`, `EDITED`, `DELETED`).
  - Threaded replies via `replyToMessageId`.
  - Emoji reactions (`MessageReaction`).
  - Invariant enforcement: Only original senders can edit message content; only senders or admins can delete messages.
- **Strongly-Typed Value Objects**: Encapsulated validation rules for `ChatRoomId` (UUID), `MessageId` (UUID), `UserId` (UUID), `MessageContent` (non-blank, max 2000 chars), `RoomTitle` (max 100 chars), and `MessageReaction`.
- **Domain Services**:
  - `ChatRoomDomainService`: Orchestrates chat room creation (preventing duplicate direct chats between same pair of users), participant management, archiving, and room deletion.
  - `MessageDomainService`: Manages message publishing, room participation validation, editing, soft-deletion, reaction tracking, and read receipts.

---

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.4 (Java 17)
- **AWS & Messaging**: AWS SDK v2 (SNS & SQS) via LocalStack
- **Database**: PostgreSQL (`chat_db` / `chat_user`) on port `5434`
- **Persistence**: Spring Data JPA / Hibernate
- **Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Testing**: JUnit 5 (Jupiter), Mockito
- **Containerization**: Docker Compose

---

## 📁 Directory & Package Structure

```
chat/
├── docker-compose.yml
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/chess/chat/
    │   │   ├── ChatApplication.java
    │   │   ├── domain/                         # Pure DDD Domain Layer (Zero Framework Dependencies)
    │   │   │   ├── exception/                  # Domain Exceptions (DomainException, ChatRoomNotFoundException, etc.)
    │   │   │   ├── model/                      # Aggregates & Value Objects (ChatRoom, Message, ChatParticipant, UserId, etc.)
    │   │   │   ├── port/                       # Event Publisher Ports (ChatEventPublisherPort)
    │   │   │   ├── repository/                 # Repository Ports (ChatRoomRepository, MessageRepository)
    │   │   │   └── service/                    # Domain Services (ChatRoomDomainService, MessageDomainService)
    │   │   └── infrastructure/                 # Infrastructure Layer
    │   │       ├── config/                     # Spring Configuration (AwsConfig, DomainServiceConfig, OpenApiConfig)
    │   │       ├── messaging/                  # AWS SNS/SQS Messaging Adapters
    │   │       ├── persistence/                # JPA Entities, Repositories & Adapters
    │   │       └── web/                        # REST Controllers, DTOs & Global Exception Handler
    │   └── resources/
    │       └── application.yml
    └── test/                                   # Domain & Infrastructure Unit Tests (46 passing)
```

---

## 🚀 Getting Started

### 1. Start Database Infrastructure Container

Spin up the dedicated PostgreSQL database container for the Chat service:

```bash
docker compose -f chat/docker-compose.yml up -d
```

| Service | Port | Database | Credentials |
|---|---|---|---|
| **PostgreSQL (Chat)** | `5434` | `chat_db` | User: `chat_user` / Pass: `chat_pass` |

### 2. Run Tests

To execute the complete unit and integration test suite across domain models, services, repository adapters, and controllers:

```bash
cd chat
mvn clean test
```

### 3. Run the Application

```bash
cd chat
mvn spring-boot:run
```

Swagger UI will be accessible at: `http://localhost:8082/swagger-ui.html`.
