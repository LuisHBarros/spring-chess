# Chat Microservice (`chat`)

A microservice for direct, group, guild, and match chat. Built with **Spring Boot 3** and **Domain-Driven Design (DDD)** architecture. Messages are delivered via REST and polling; there is no WebSocket implementation.

---

## Features

- **Domain-Driven Design (DDD)**: Pure domain layer with aggregate roots, entities, value objects, domain services, and repository ports separated from infrastructure adapters.
- **Aggregate Roots**:
  - `ChatRoom`: manages participants, room type, status, and lifecycle.
  - `Message`: represents a single chat message with content, type, status, reactions, and sequence.
- **Room Types** (`ChatRoomType`): `DIRECT`, `GROUP`, `GUILD`, `MATCH`.
- **Participant Roles** (`ParticipantRole`): `OWNER`, `ADMIN`, `MEMBER`.
- **Message Types** (`MessageType`): `TEXT`, `SYSTEM`, `IMAGE`, `GAME_INVITE`, `CHESS_MOVE_ANNOTATION`.
- **Message Statuses** (`MessageStatus`): `SENT`, `DELIVERED`, `READ`, `EDITED`, `DELETED`.
- **Message Sequence & Ordering**:
  - Each message carries a monotonically increasing `sequence` number per room.
  - The next sequence is computed with `findTopByChatRoomIdOrderBySequenceDesc`.
  - Message retrieval uses ordering by `(sentAt, sequence)`: room messages are sorted `sentAt DESC, sequence DESC`; unread messages are sorted `sentAt ASC, sequence ASC`.
- **`CHAT_ACCESS` Permission Check**:
  - Before sending a message to a `GUILD` room, `MessageDomainService` asks the `social` microservice whether the sender has `CHAT_ACCESS` for the referenced guild.
- **AWS SNS & SQS Asynchronous Messaging**:
  - Publishes events to the `chat-events` SNS topic.
  - Polls the `chat-user-events-queue` SQS queue on a fixed 5-second schedule.
  - W3C `traceId` is propagated via SNS/SQS message attributes.
- **Swagger / OpenAPI**:
  - API documentation is available at `http://localhost:8082/swagger-ui.html`.

---

## Technology Stack

- **Framework**: Spring Boot 3.2.4 (Java 17)
- **Persistence**: Spring Data JPA / Hibernate
- **Database**: PostgreSQL (`chat_db`) for local development
- **AWS & Messaging**: AWS SDK v2 (SNS & SQS) via LocalStack
- **Testing**: JUnit 5 (Jupiter), Mockito
- **Containerization**: Docker Compose

---

## Directory & Package Structure

```
chat/
├── docker-compose.yml
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/chess/chat/
    │   │   ├── ChatApplication.java
    │   │   ├── domain/
    │   │   │   ├── exception/
    │   │   │   ├── model/          # Aggregates, Entities, Value Objects
    │   │   │   ├── port/           # Event Publisher & Guild Permission Ports
    │   │   │   ├── repository/     # Repository Ports
    │   │   │   └── service/        # Domain Services (ChatRoom, Message)
    │   │   └── infrastructure/
    │   │       ├── client/         # Social Guild Permission Adapter
    │   │       ├── config/         # OpenApiConfig, SecurityConfig, AwsConfig
    │   │       ├── messaging/      # SNS Publisher & SQS Listener
    │   │       ├── persistence/    # JPA Entities, Adapters, Spring Data Repositories
    │   │       └── web/            # Controllers, DTOs, GlobalExceptionHandler
    │   └── resources/
    │       └── application.yml
    └── test/
        └── resources/
```

---

## Getting Started

### 1. Environment Variables

The service imports the root `.env` file. Ensure the following variable is set before starting the service:

```dotenv
CHAT_DB_PASSWORD=chat_pass
```

This value is used by `chat/src/main/resources/application.yml` (`spring.datasource.password`).

### 2. Start Local Infrastructure

```bash
docker compose -f chat/docker-compose.yml up -d
```

The `chat/docker-compose.yml` starts a dedicated `chat-db` PostgreSQL container with this port mapping:

| Host Port | Container Port | Service         |
|-----------|----------------|-----------------|
| `5434`    | `5432`         | PostgreSQL Chat |

### 3. Run the Application

```bash
cd chat
mvn spring-boot:run
```

The service listens on port `8082`.

### 4. Swagger UI

Open `http://localhost:8082/swagger-ui.html` to browse the REST API.

---

## Domain Architecture

### Aggregates

#### `ChatRoom` (Aggregate Root)

- **Identity**: `ChatRoomId`
- **Value Objects**: `RoomTitle`, `UserId`
- **Entities**: `ChatParticipant`
- **Types**: `DIRECT`, `GROUP`, `GUILD`, `MATCH`
- **Statuses**: `ACTIVE`, `ARCHIVED`
- **Key Operations**:
  - `createDirect(...)`: one-to-one chat between two distinct users.
  - `createGroup(...)`: multi-user chat with a creator and optional members.
  - `createGuildChannel(...)`: chat room linked to a guild via `targetReferenceId` (the guild ID).
  - `createMatchChat(...)`: chat room for an in-progress chess match.
  - `addParticipant(...)`, `removeParticipant(...)`, `updateParticipantRole(...)`, `updateTitle(...)`, `archive(...)`, `activate(...)`.

#### `Message` (Aggregate Root)

- **Identity**: `MessageId`
- **Value Objects**: `MessageContent`, `UserId`, `ChatRoomId`
- **Entities / Collections**: `MessageReaction`
- **Key Fields**:
  - `sentAt`: the wall-clock timestamp of the message.
  - `sequence`: a per-room monotonic ordering value used together with `sentAt` for stable message ordering.
  - `replyToMessageId`: optional parent message for threaded replies.
- **Key Operations**:
  - `send(...)`: creates a new message with `SENT` status.
  - `edit(actorId, newContent)`: only the original sender can edit; status becomes `EDITED`.
  - `delete(actorId)`: soft delete; content is replaced and status becomes `DELETED`.
  - `addReaction(...)`, `removeReaction(...)`, `markAsDelivered()`, `markAsRead()`.

### Room Types

- `DIRECT`: two participants only. Adding participants is not allowed.
- `GROUP`: multi-user chat with `OWNER`/`ADMIN`/`MEMBER` roles.
- `GUILD`: guild channel backed by the `social` microservice. The sender must have `CHAT_ACCESS` permission in the referenced guild.
- `MATCH`: chat between two players in a specific chess match.

### Message Sequence & `(sentAt, sequence)` Ordering

- `MessageDomainService.sendMessage` computes the next room sequence by reading the current maximum sequence value:

```java
long nextSequence = messageRepository.findTopByChatRoomIdOrderBySequenceDesc(chatRoomId)
        .map(Message::getSequence)
        .orElse(0L) + 1;
```

- The repository exposes queries that order by both `sentAt` and `sequence`:
  - `findByChatRoomIdOrderBySentAtDescSequenceDesc` for paginated room history.
  - `findUnreadMessages` uses `ORDER BY m.sentAt ASC, m.sequence ASC`.

Using the `(sentAt, sequence)` tuple guarantees a stable, chronological ordering even when two messages share the same `sentAt` timestamp.

### `CHAT_ACCESS` Permission Check

When a user sends a message to a `GUILD` room, `MessageDomainService` delegates to `GuildPermissionPort`:

```java
if (chatRoom.getType() == ChatRoomType.GUILD) {
    if (!guildPermissionPort.hasChatAccess(senderId, chatRoom.getTargetReferenceId())) {
        throw new UnauthorizedChatOperationException("Sender ... does not have CHAT_ACCESS permission for this guild");
    }
}
```

The default implementation, `SocialGuildPermissionAdapter`, calls the `social` microservice endpoint:

```
GET {social.service.url}/api/v1/guilds/{guildId}/members/{userId}/permissions?permission=CHAT_ACCESS
```

If the call fails or the permission is denied, the message is rejected.

### SNS / SQS Event Flow

```
[chat-events SNS topic]
        |
        | publish
        v
[AwsSnsChatEventPublisher]

[chat-user-events-queue]  <-- polled by --> [ChatUserEventListener]
```

- `AwsSnsChatEventPublisher` publishes events such as `MESSAGE_SENT`, `MESSAGE_EDITED`, and `MESSAGE_DELETED` to the `chat-events` SNS topic configured by `aws.sns.chat-events-topic-arn`.
- `ChatUserEventListener` polls the `chat-user-events-queue` on a `@Scheduled(fixedDelay = 5000)` loop and propagates the `traceId` message attribute.

### Messaging via REST & Polling

This microservice does **not** use WebSockets. Clients send and retrieve messages through the REST endpoints in `MessageController` and `ChatRoomController` (e.g., `GET /api/v1/chat/messages/room/{roomId}`). Real-time delivery is not supported; clients must poll for new messages.

---

## Migration Note: New `sequence` Column

The `Message` aggregate and the `MessageJpaEntity` table (`chat_messages`) now require a non-nullable `sequence` column:

```java
@Column(name = "sequence", nullable = false)
private long sequence;
```

- The domain constructor validates that `sequence >= 0`.
- JPA/Hibernate `ddl-auto: update` will create the column automatically for new local deployments.
- For existing production databases, add the `sequence` column with a default value of `0` and backfill existing rows before marking it `NOT NULL`.

---

## Configuration

Key settings in `chat/src/main/resources/application.yml`:

```yaml
server:
  port: 8082

spring:
  datasource:
    url: jdbc:postgresql://localhost:5434/chat_db
    username: chat_user
    password: ${CHAT_DB_PASSWORD}

social:
  service:
    url: ${SOCIAL_SERVICE_URL:http://localhost:8081}

aws:
  sns:
    chat-events-topic-arn: ${AWS_CHAT_EVENTS_TOPIC_ARN:arn:aws:sns:us-east-1:000000000000:chat-events}
  sqs:
    chat-user-events-queue-url: ${AWS_CHAT_USER_EVENTS_QUEUE_URL:http://localhost:4566/000000000000/chat-user-events-queue}
```
