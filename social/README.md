# Social Microservice (`social`)

A microservice for user social interactions, friendships, guilds, customized categories & ranks, and guild ownership. Built with **Spring Boot 3** and **Domain-Driven Design (DDD)** architecture.

---

## Features

- **Domain-Driven Design (DDD)**: Pure domain layer with aggregate roots, entities, value objects, domain services, and repository ports separated from infrastructure adapters.
- **Aggregate Roots**:
  - `Guild`: owns `GuildCategory`, `GuildRank`, and `GuildMember` entities.
  - `Friendship`: manages friend-request lifecycle between two users.
- **Strongly-Typed Value Objects**:
  - `UserId`, `GuildId`, `CategoryId`, `RankId`, `FriendshipId`
  - `GuildName`, `CategoryName`, `RankName`, `Avatar`
- **Guild Permissions & Ranks**:
  - `GuildRole`: `OWNER`, `OFFICER`, `MEMBER`.
  - `RankPermission`: `MANAGE_RANKS`, `MANAGE_CATEGORIES`, `MANAGE_MEMBERS`, `INVITE_MEMBERS`, `KICK_MEMBERS`, `POST_ANNOUNCEMENTS`, `CHAT_ACCESS`.
  - Custom `GuildRank` instances are created inside `GuildCategory` and define which permissions a member has.
- **`CHAT_ACCESS` Permission**:
  - The `chat` microservice calls `GET /api/v1/guilds/{guildId}/members/{userId}/permissions?permission=CHAT_ACCESS` to verify that a guild member is allowed to send messages in a guild channel.
- **AWS SNS & SQS Asynchronous Messaging**:
  - Publishes events to the `social-events` SNS topic.
  - Polls the `social-user-events-queue` and `social-match-events-queue.fifo` SQS queues on a fixed 5-second schedule.
  - W3C `traceId` is propagated via SNS/SQS message attributes.
- **TOCTOU Race Handling**:
  - `FriendshipDomainService` and `GuildDomainService` use database-level `DataIntegrityViolationException` handling to guard against check-then-act races during concurrent friendship/guild creation.
- **H2 Test Datasource**:
  - Unit and integration tests run against an in-memory H2 database (`social/src/test/resources/application.yml`).
- **Swagger / OpenAPI**:
  - API documentation is available at `http://localhost:8081/swagger-ui.html`.

---

## Technology Stack

- **Framework**: Spring Boot 3.2.4 (Java 17)
- **Persistence**: Spring Data JPA / Hibernate
- **Database**: PostgreSQL (`social_db`) for local development; H2 for tests
- **AWS & Messaging**: AWS SDK v2 (SNS & SQS) via LocalStack
- **Testing**: JUnit 5 (Jupiter), Mockito
- **Containerization**: Docker Compose

---

## Directory & Package Structure

```
social/
├── docker-compose.yml
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/chess/social/
    │   │   ├── SocialApplication.java
    │   │   ├── domain/
    │   │   │   ├── exception/
    │   │   │   ├── model/          # Aggregates, Entities, Value Objects
    │   │   │   ├── repository/     # Repository Ports
    │   │   │   └── service/        # Domain Services
    │   │   └── infrastructure/
    │   │       ├── config/         # OpenApiConfig, SecurityConfig, AwsConfig
    │   │       ├── messaging/      # SNS Publisher & SQS Listeners
    │   │       ├── persistence/    # JPA Entities, Adapters, Spring Data Repositories
    │   │       └── web/            # Controllers, DTOs, GlobalExceptionHandler
    │   └── resources/
    │       └── application.yml
    └── test/
        └── resources/
            └── application.yml     # H2 in-memory datasource
```

---

## Getting Started

### 1. Environment Variables

The service imports the root `.env` file. Ensure the following variable is set before starting the service:

```dotenv
SOCIAL_DB_PASSWORD=social_pass
```

This value is used by `social/src/main/resources/application.yml` (`spring.datasource.password`).

### 2. Start Local Infrastructure

```bash
docker compose -f social/docker-compose.yml up -d
```

The `social/docker-compose.yml` starts LocalStack and maps the following port:

| Host Port | Container Port | Service                  |
|-----------|----------------|--------------------------|
| `5433`    | `5432`         | LocalStack RDS PostgreSQL |

### 3. Run the Application

```bash
cd social
mvn spring-boot:run
```

The service listens on port `8081`.

### 4. Swagger UI

Open `http://localhost:8081/swagger-ui.html` to browse the REST API.

---

## Domain Architecture

### Aggregates

#### `Guild` (Aggregate Root)

- **Identity**: `GuildId`
- **Entities**: `GuildCategory` (contains `GuildRank`), `GuildMember`
- **Value Objects**: `GuildName`, `UserId`, `Avatar`, `CategoryName`, `RankName`
- **Key Operations**:
  - `create(...)`: creates the guild, adds the creator as `OWNER`, and initializes a default `General` category with `Leader` (all permissions) and `Member` (`CHAT_ACCESS`) ranks.
  - `transferOwnership(actorId, newOwnerId)`: only the owner can transfer ownership; the old owner becomes `OFFICER`.
  - `addCategory(...)`, `addRankToCategory(...)`, `addMember(...)`, `removeMember(...)`, `assignRankToMember(...)`.

#### `Friendship` (Aggregate Root)

- **Identity**: `FriendshipId`
- **Value Objects**: `UserId` (requester and addressee)
- **Status**: `PENDING`, `ACCEPTED`, `DECLINED`, `BLOCKED`
- **Key Operations**:
  - `request(requesterId, addresseeId)`: validates the request, preventing self-friendship.
  - `accept(actorId)`: only the addressee can accept a pending request.
  - `decline(actorId)`: only the addressee can decline.
  - `block(actorId)`: either participant can block; only the blocking user can unblock.

### Guild Permissions & Ranks

- `OWNER` and `OFFICER` roles implicitly have all permissions.
- A `GuildMember` may optionally be assigned a `GuildRank`. The rank's `RankPermission` set determines what the member can do.
- `CHAT_ACCESS` is the permission used by the `chat` microservice to decide whether a user can send messages in a guild chat room.

### TOCTOU Race Handling

Domain services guard against Time-of-Check to Time-of-Use races by catching database constraint violations on save:

- **`FriendshipDomainService.sendFriendRequest` / `blockUser`**: first checks for an existing friendship, then attempts to save. If a `DataIntegrityViolationException` occurs because another request was inserted concurrently, the service translates it into `FriendshipAlreadyExistsException`.
- **`GuildDomainService.createGuild`**: first checks `existsByName(...)`, then attempts to save. A concurrent `DataIntegrityViolationException` is translated into `DuplicateCategoryException` with the message "A guild with name ... already exists".

### SNS / SQS Event Flow

```
[social-events SNS topic]
        |
        | publish
        v
[AwsSnsSocialEventPublisher]

[social-user-events-queue]  <-- polled by --> [SocialUserEventListener]
[social-match-events-queue.fifo] <-- polled by --> [SocialMatchEventListener]
```

- `AwsSnsSocialEventPublisher` (`infrastructure/messaging/AwsSnsSocialEventPublisher.java`) publishes events to the `social-events` SNS topic configured by `aws.sns.social-events-topic-arn`.
- `SocialUserEventListener` polls the `social-user-events-queue`.
- `SocialMatchEventListener` polls the `social-match-events-queue.fifo` queue (FIFO).
- Both listeners run on a `@Scheduled(fixedDelay = 5000)` poll loop and propagate the `traceId` message attribute.

### Test Datasource

`social/src/test/resources/application.yml` overrides the datasource to an H2 in-memory database:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:social_test_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
```

---

## Continuous Integration

The current CI pipeline (`.github/workflows/ci.yml` and `.github/workflows/build.yml`) builds and tests only the `auth` and `social` services. The `chat`, `game`, and `observation` services are not yet executed in CI.
