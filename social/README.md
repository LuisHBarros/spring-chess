# Social Microservice (`social`)

A microservice for User Social Interactions, Friendships, Guilds, Customized Categories & Ranks, and Guild Ownership built with **Spring Boot 3** using **Domain-Driven Design (DDD)** architecture.

---

## 🌟 Features

- **Domain-Driven Design (DDD)**: Clean architecture separating Domain models, Value Objects, Domain Services, and Repository Ports from Infrastructure adapters.
- **Friendship Aggregate Root**:
  - Send, accept, decline, block, unblock, and remove friendships.
  - Invariant rules: Prevents self-friendship requests, duplicate active requests, and enforces strict transition permissions.
- **Guild Aggregate Root**:
  - **Guild Owner**: Every Guild maintains an explicit Owner (the creator).
  - **Ownership Transfer**: Guild owners can transfer ownership to any member of the guild.
  - **Member Management**: Add/remove members, update member roles (`OWNER`, `OFFICER`, `MEMBER`), and assign ranks.
- **Customized Categories & Ranks (Entities)**:
  - **Guild Categories**: Guilds can create and customize categories (`GuildCategory`) to organize ranks and topics.
  - **Custom Ranks**: Ranks (`GuildRank`) defined within categories with priority levels (1 = highest) and granular permissions (`MANAGE_RANKS`, `MANAGE_CATEGORIES`, `MANAGE_MEMBERS`, `INVITE_MEMBERS`, `KICK_MEMBERS`, `POST_ANNOUNCEMENTS`, `CHAT_ACCESS`).
- **Strongly-Typed Value Objects**: Encapsulated validation rules for `UserId` (UUID), `GuildId`, `CategoryId`, `RankId`, `FriendshipId`, `GuildName`, `CategoryName`, and `RankName`.
- **AWS SNS & SQS Asynchronous Messaging**:
  - Event Publisher: Publishes `social-events` to SNS with W3C `traceId` context propagation.
  - Event Listeners: Consumes `social-user-events-queue` and `social-match-events-queue.fifo` SQS queues.
- **Isolated Database & LocalStack RDS**:
  - Uses dedicated database credentials (`social_user` / `social_pass`) and database `social_db` on LocalStack RDS.
- **Comprehensive Unit Test Suite**: 39 unit tests covering all domain entities, value objects, state transitions, domain services, and messaging adapters.

---

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.4 (Java 17)
- **AWS & Messaging**: AWS SDK v2 (SNS & SQS) via LocalStack
- **Database**: LocalStack RDS (`social_db` / `social_user`)
- **Persistence Framework**: Spring Data JPA / Hibernate
- **Testing**: JUnit 5 (Jupiter), Mockito
- **Containerization**: Docker Compose

---

## 📁 Directory & Package Structure

```
social/
├── docker-compose.yml
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/chess/social/
    │   │   ├── SocialApplication.java
    │   │   └── domain/                         # Pure DDD Domain Layer (Zero Framework Dependencies)
    │   │       ├── exception/                  # Domain Exceptions (DomainException, GuildNotFoundException, etc.)
    │   │       ├── model/                      # Entities & Value Objects (Guild, Friendship, GuildCategory, GuildRank, UserId, etc.)
    │   │       ├── repository/                 # Repository Ports (GuildRepository, FriendshipRepository)
    │   │       └── service/                    # Domain Services (GuildDomainService, FriendshipDomainService)
    │   └── resources/
    │       └── application.yml
    └── test/                                   # Domain Unit Tests (36 tests passing)
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+ JDK installed
- Maven 3.8+ installed
- Docker & Docker Compose installed

### 1. Start Local Infrastructure Containers

Spin up the dedicated PostgreSQL database container for the Social service:

```bash
docker compose -f social/docker-compose.yml up -d
```

| Service | Port | Database | Credentials |
|---|---|---|---|
| **PostgreSQL (Social)** | `5433` | `social_db` | User: `social_user` / Pass: `social_pass` |

### 2. Run Domain Tests

To execute the unit test suite across domain models and services:

```bash
cd social
mvn clean test
```

### 3. Run the Application

```bash
cd social
mvn spring-boot:run
```

---

## 🏛️ Domain Architecture Deep Dive

### Aggregates & Entities

#### 1. `Guild` (Aggregate Root)
- **Identity**: `GuildId`
- **Attributes**: `GuildName`, `description`, `UserId ownerId`, `List<GuildCategory> categories`, `List<GuildMember> members`, `createdAt`, `updatedAt`
- **Key Business Operations**:
  - `create(GuildName, description, creatorId)`: Automatically sets creator as Owner and initializes default "General" category & default ranks.
  - `transferOwnership(actorId, newOwnerId)`: Verifies `actorId` is the current owner and transfers ownership to `newOwnerId`.
  - `addCategory(actorId, categoryName, description)`: Adds a new category after checking authorization.
  - `addRankToCategory(actorId, categoryId, rankName, priority, permissions)`: Adds a custom rank to a specific category.
  - `addMember(...)`, `removeMember(...)`, `assignRankToMember(...)`.

#### 2. `Friendship` (Aggregate Root)
- **Identity**: `FriendshipId`
- **Attributes**: `UserId requesterId`, `UserId addresseeId`, `FriendshipStatus status`, `UserId actionUserId`, `createdAt`, `updatedAt`
- **Statuses**: `PENDING`, `ACCEPTED`, `DECLINED`, `BLOCKED`
- **Key Business Operations**:
  - `request(requesterId, addresseeId)`: Validates that requester and addressee are different users and sets status to `PENDING`.
  - `accept(actorId)`: Only addressee can accept.
  - `decline(actorId)`: Only addressee can decline.
  - `block(actorId)`: Either participant can block.
  - `unblock(actorId)`: Only the user who applied the block can unblock.

#### 3. `GuildCategory` (Entity)
- **Identity**: `CategoryId`
- **Attributes**: `CategoryName`, `description`, `List<GuildRank> ranks`
- **Key Operations**: `addRank(...)`, `removeRank(...)`, `findRank(...)`.

#### 4. `GuildRank` (Entity)
- **Identity**: `RankId`
- **Attributes**: `RankName`, `int priority`, `Set<RankPermission> permissions`

#### 5. `GuildMember` (Entity)
- **Attributes**: `UserId`, `GuildRole` (`OWNER`, `OFFICER`, `MEMBER`), `RankId assignedRankId`, `joinedAt`

---

## 🧪 Domain Services

- **`FriendshipDomainService`**: Handles end-to-end friendship creation, acceptance, declining, blocking, and removing relationships while checking existing relationship states.
- **`GuildDomainService`**: Orchestrates guild creation with unique name checks, ownership transfers, category/rank management, and member enrollment.
