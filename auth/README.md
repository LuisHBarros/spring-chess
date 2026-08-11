# Authentication Microservice (`auth`)

A production-ready Authentication & User Management microservice built with **Spring Boot 3** using **Domain-Driven Design (DDD)** architecture.

---

## 🌟 Features

- **Domain-Driven Design (DDD)**: Clean architecture separating Domain models, Value Objects, Domain Services, and Ports from Infrastructure adapters.
- **Strongly-Typed Value Objects**: Encapsulated validation rules for `UserId` (UUID), `Username`, `Email`, and `Password`.
- **JWT & Refresh Tokens**: Secure authentication issuing short-lived Access Tokens and long-lived Refresh Tokens using JJWT.
- **Redis Token Blacklist**: Instant access token revocation (Logout) backed by Redis TTL key expiration.
- **Password Hashing & Security**: Passwords hashed using Spring Security's **BCryptPasswordEncoder**.
- **Password Recovery**: Tokenized password reset workflow dispatches emails via **MailHog** SMTP.
- **PostgreSQL Persistence**: Spring Data JPA repository adapters with `UserJpaEntity` mappings.
- **REST Presentation Layer**: Clean `/api/v1/auth` REST API with DTO mappings and global exception handling.
- **80% Code Coverage Enforcement**: JaCoCo Maven plugin enforcing minimum 80% line coverage threshold during `mvn verify`.
- **GitHub Actions CI/CD Pipeline**: Continuous integration with live PostgreSQL, Redis, and MailHog Docker service containers.

---

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.4 (Java 17 / 21)
- **Database**: PostgreSQL 16
- **In-Memory Cache**: Redis 7
- **Email Server (Dev)**: MailHog
- **Security & JWT**: Spring Security, BCrypt, JJWT 0.12.5
- **Testing**: JUnit 5 (Jupiter), Mockito, H2 Database (in-memory test profile)
- **Code Coverage**: JaCoCo
- **Containerization**: Docker Compose
- **CI/CD**: GitHub Actions

---

## 📁 Directory & Package Structure

```
auth/
├── docker-compose.yml
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/chess/auth/
    │   │   ├── AuthApplication.java
    │   │   ├── domain/                         # Pure DDD Domain Layer (No Framework Dependencies)
    │   │   │   ├── exception/                  # Domain exceptions (DomainException, UserAlreadyExistsException, etc.)
    │   │   │   ├── model/                      # Entity (User) & Value Objects (UserId, Username, Email, Password, AuthToken)
    │   │   │   ├── port/                       # Domain ports/interfaces (UserRepository, TokenProvider, TokenBlacklistService, etc.)
    │   │   │   └── service/                    # Domain services (UserRegistrationService, UserLoginService, TokenAuthenticationService, etc.)
    │   │   └── infrastructure/                 # Infrastructure Adapters & Technical Framework Details
    │   │       ├── config/                     # Spring Configuration Beans & Security Config
    │   │       ├── email/                      # JavaMailServiceAdapter (Spring Mail / MailHog)
    │   │       ├── persistence/                # JPA Entity, SpringDataUserRepository, UserRepositoryAdapter (PostgreSQL)
    │   │       ├── security/                   # BCryptPasswordEncoderAdapter, JwtTokenProviderAdapter, RedisTokenBlacklistAdapter
    │   │       └── web/                        # REST Controllers (AuthController), DTOs, GlobalExceptionHandler
    │   └── resources/
    │       └── application.yml
    └── test/                                   # Unit, Integration & WebMvc Tests (50+ tests)
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+ JDK installed
- Maven 3.8+ installed
- Docker & Docker Compose installed

### 1. Start Local Infrastructure Containers

Spin up PostgreSQL, Redis, and MailHog containers using Docker Compose:

```bash
docker compose -f auth/docker-compose.yml up -d
```

| Service | Port | Description |
|---|---|---|
| **PostgreSQL** | `5432` | Database (`auth_db` / `auth_user` / `auth_pass`) |
| **Redis** | `6379` | Token Blacklist Cache |
| **MailHog SMTP** | `1025` | SMTP Server |
| **MailHog Web UI** | `8025` | Web interface to view sent emails (`http://localhost:8025`) |

### 2. Run the Application

```bash
cd auth
mvn spring-boot:run
```

The server will start on `http://localhost:8080`.

---

## 🧪 Testing & Code Coverage

Run unit & integration tests and enforce the **80% JaCoCo coverage threshold**:

```bash
cd auth
mvn clean verify
```

The JaCoCo HTML report will be generated at `auth/target/site/jacoco/index.html`.

---

## 📡 REST API Reference (`/api/v1/auth`)

### 1. Register User
- **HTTP Method**: `POST`
- **Path**: `/api/v1/auth/register`
- **Request Body**:
  ```json
  {
    "username": "grandmaster",
    "email": "gm@chess.com",
    "password": "StrongPassword123"
  }
  ```
- **Response**: `201 Created`
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresInSeconds": 3600,
    "user": {
      "id": "e6a2b3c4-...",
      "username": "grandmaster",
      "email": "gm@chess.com",
      "createdAt": "2026-08-11T10:00:00Z",
      "lastSeenAt": "2026-08-11T10:00:00Z"
    }
  }
  ```

### 2. Login User
- **HTTP Method**: `POST`
- **Path**: `/api/v1/auth/login`
- **Request Body**:
  ```json
  {
    "email": "gm@chess.com",
    "password": "StrongPassword123"
  }
  ```
- **Response**: `200 OK` (returns JWT token pair and user details)

### 3. Refresh Access Token
- **HTTP Method**: `POST`
- **Path**: `/api/v1/auth/refresh`
- **Request Body**:
  ```json
  {
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
  }
  ```
- **Response**: `200 OK` (returns new access and refresh token pair)

### 4. Logout User (Revoke Token)
- **HTTP Method**: `POST`
- **Path**: `/api/v1/auth/logout`
- **Header**: `Authorization: Bearer <access_token>`
- **Response**: `200 OK`
  ```json
  {
    "success": true,
    "message": "Logged out successfully"
  }
  ```

### 5. Initiate Password Recovery
- **HTTP Method**: `POST`
- **Path**: `/api/v1/auth/recover-password`
- **Request Body**:
  ```json
  {
    "email": "gm@chess.com"
  }
  ```
- **Response**: `200 OK` (dispatches recovery token email to MailHog)

### 6. Reset Password
- **HTTP Method**: `POST`
- **Path**: `/api/v1/auth/reset-password`
- **Request Body**:
  ```json
  {
    "email": "gm@chess.com",
    "token": "recovery-token-uuid",
    "newPassword": "NewSecretPassword123"
  }
  ```
- **Response**: `200 OK`

---

## 🔄 CI/CD Pipeline

Continuous Integration is automated via GitHub Actions in [`.github/workflows/ci.yml`](file:///.github/workflows/ci.yml):
- Provisions live Docker service containers for PostgreSQL, Redis, and MailHog.
- Runs `mvn clean verify` to build the application and execute all tests.
- Fails the build if line coverage drops below **80%**.
- Uploads JaCoCo coverage report artifacts.
