# Authentication Microservice (`auth`)

A production-ready Authentication & User Management microservice built with **Spring Boot 3** using **Domain-Driven Design (DDD)** architecture. It runs on port **8080** and is the first service built and verified in the CI pipeline.

---

## Features

- **Domain-Driven Design (DDD)**: Clean architecture separating Domain models, Value Objects, Domain Services, and Ports from Infrastructure adapters.
- **Strongly-Typed Value Objects**: Encapsulated validation rules for `UserId` (UUID), `Username` (3–30 letters/numbers/underscores), `Email`, and `Password`.
- **RSA-Signed JWT & Refresh Tokens**: Access tokens are short-lived (1 hour) and refresh tokens are long-lived (7 days). Tokens are signed with an RSA key pair (`RS256`) and include a key ID (`kid=auth-key-1`) for JWKS-based verification.
- **Refresh Token Rotation & Invalidation**: Refreshing a token issues a **new** token pair and increments the user's `refreshTokenVersion`. Any password change also increments the version, immediately invalidating all outstanding refresh tokens.
- **Redis Token Blacklist**: Instant access-token revocation on logout, backed by Redis TTL key expiration.
- **Password Hashing & Security**: Passwords hashed using Spring Security's **BCryptPasswordEncoder**.
- **Password Policy**: Enforced regex `^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$` — at least one uppercase letter, one digit, one special character, and 8+ characters total.
- **Password Recovery**: Tokenized password reset workflow dispatches emails via **MailHog** SMTP.
- **PostgreSQL Persistence**: Spring Data JPA repository adapters with `UserJpaEntity` mappings connected to LocalStack RDS.
- **AWS SNS/SQS Asynchronous Communication**:
  - Publishes user lifecycle events to the SNS topic `user-events`.
  - Listens for match events from the SQS FIFO queue `auth-match-events-queue.fifo`, which is subscribed to the SNS FIFO topic `match-events.fifo`.
- **REST Presentation Layer**: Clean `/api/v1/auth` REST API with DTO mappings and global exception handling.
- **API Documentation**: Interactive Swagger UI powered by SpringDoc OpenAPI 3.0, with schema examples and JWT auth support.
- **JSON Web Key Set (JWKS)**: Public RSA key exposed so other services can verify auth-issued JWTs without sharing secrets.
- **Rate-Limiting on Login**: Redis-backed per-identity rate limiter (5 attempts / 60 seconds) with an in-memory fallback.
- **Health Check**: Custom `AuthHealthIndicator` that probes PostgreSQL and Redis connectivity, exposed via `/actuator/health`.
- **Full Observability & Telemetry**: Integrated Spring Boot Actuator, Prometheus metrics endpoint (`/actuator/prometheus`), OpenTelemetry OTLP tracing export to Tempo, and structured JSON logs for Loki.
- **80% Code Coverage Enforcement**: JaCoCo Maven plugin enforcing minimum 80% line coverage threshold during `mvn verify`.
- **GitHub Actions CI/CD Pipeline**: Continuous integration with live LocalStack (RDS/SNS/SQS), Redis, and MailHog Docker service containers.

---

## Technology Stack

- **Framework**: Spring Boot 3.2.4 (Java 17)
- **AWS & Messaging**: AWS SDK v2 (SNS & SQS) via LocalStack
- **Database**: LocalStack RDS (PostgreSQL 16)
- **In-Memory Cache**: Redis 7
- **Email Server (Dev)**: MailHog
- **Security & JWT**: Spring Security, BCrypt, JJWT 0.12.5, RSA `RS256`
- **API Documentation**: SpringDoc OpenAPI 2.5.0 (Swagger UI)
- **Observability**: Spring Boot Actuator, Micrometer, Prometheus, OpenTelemetry (OTLP)
- **Testing**: JUnit 5 (Jupiter), Mockito, H2 Database (in-memory test profile)
- **Code Coverage**: JaCoCo
- **Containerization**: Docker Compose
- **CI/CD**: GitHub Actions

---

## Directory & Package Structure

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
    │   │       ├── config/                     # Spring Config, Security Config, OpenAPI Config, Health Indicator
    │   │       ├── email/                      # JavaMailServiceAdapter (Spring Mail / MailHog)
    │   │       ├── persistence/                # JPA Entity, SpringDataUserRepository, UserRepositoryAdapter (PostgreSQL)
    │   │       ├── security/                   # BCryptPasswordEncoderAdapter, JwtTokenProviderAdapter, RedisTokenBlacklistAdapter, RateLimiterService, RsaKeyPairProvider
    │   │       └── web/                        # REST Controllers (AuthController, JwksController), DTOs, GlobalExceptionHandler
    │   └── resources/
    │       └── application.yml
    └── test/                                   # Unit, Integration & WebMvc Tests
```

---

## Getting Started

### Prerequisites

- Java 17+ JDK installed
- Maven 3.8+ installed
- Docker & Docker Compose installed

### 1. Configure the Root `.env` File

Create a `.env` file in the repository **root** (next to the `auth/` directory). `application.yml` loads it via `spring.config.import: optional:file:../.env[.properties]`.

```properties
AUTH_DB_PASSWORD=auth_pass
```

> `AUTH_DB_PASSWORD` is used both by the Spring Boot datasource and by the LocalStack RDS init script (`localstack/init-aws.sh`).

### 2. Start Local Infrastructure Containers

Spin up LocalStack (PostgreSQL, SNS, SQS), Redis, and MailHog containers using Docker Compose:

```bash
docker compose -f auth/docker-compose.yml up -d
```

| Service | Port | Description |
|---|---|---|
| **LocalStack Gateway** | `4566` | SNS, SQS, S3, RDS endpoints |
| **PostgreSQL** | `5432` | Database (`auth_db` / `auth_user` / `AUTH_DB_PASSWORD`) |
| **Redis** | `6379` | Token Blacklist & Login Rate-Limit Cache |
| **MailHog SMTP** | `1025` | SMTP Server |
| **MailHog Web UI** | `8025` | Web interface to view sent emails (`http://localhost:8025`) |

### 3. Run the Application

```bash
cd auth
mvn spring-boot:run
```

The server will start on `http://localhost:8080`.

### 4. Access API Documentation

Once the application is running, open the interactive Swagger UI:

| URL | Description |
|-----|-------------|
| [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html) | Interactive Swagger UI |
| [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs) | Raw OpenAPI 3.0 JSON spec |

The Swagger UI includes an **Authorize** button for entering a JWT Bearer token to test authenticated endpoints.

---

## Testing & Code Coverage

Run unit & integration tests and enforce the **80% JaCoCo coverage threshold**:

```bash
cd auth
mvn clean verify
```

The JaCoCo HTML report will be generated at `auth/target/site/jacoco/index.html`.

---

## Health Check & Observability

### Health Check

```
GET /actuator/health
```

Returns the service health status including custom checks for **PostgreSQL** and **Redis** connectivity:

```json
{
  "status": "UP",
  "components": {
    "authService": {
      "status": "UP",
      "details": {
        "database": "UP",
        "redis": "UP"
      }
    },
    "db": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

### Observability Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Service health with DB & Redis checks |
| `/actuator/prometheus` | Prometheus metrics (histograms, counters) |
| `/actuator/metrics` | Micrometer metrics browser |
| `/actuator/info` | Application info |

### Tracing

Distributed tracing is exported via **OpenTelemetry OTLP** to the configured endpoint (default: `http://localhost:4318/v1/traces`). Sampling probability is set to `1.0` (100%) by default.

---

## REST API Reference (`/api/v1/auth`)

> **Tip**: All endpoints below are also available interactively via [Swagger UI](http://localhost:8080/swagger-ui.html) with request/response examples.
>
> **Password policy**: all new or reset passwords must satisfy `^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$`. Valid examples: `Password123!`, `NewSecretPass123!`. `StrongPassword123` is **not** valid because it lacks a special character.

### 1. Register User
- **HTTP Method**: `POST`
- **Path**: `/api/v1/auth/register`
- **Request Body**:
  ```json
  {
    "username": "grandmaster",
    "email": "gm@chess.com",
    "password": "Password123!"
  }
  ```
- **Response**: `201 Created`
  ```json
  {
    "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJSUzI1NiJ9...",
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
- **Notes**:
  - The service checks for existing email/username before saving and is annotated with `@Transactional`.
  - A unique-constraint race condition is caught via `DataIntegrityViolationException` and mapped to `409 Conflict` (`UserAlreadyExistsException`).

### 2. Login User
- **HTTP Method**: `POST`
- **Path**: `/api/v1/auth/login`
- **Request Body**:
  ```json
  {
    "email": "gm@chess.com",
    "password": "Password123!"
  }
  ```
- **Response**: `200 OK` (returns JWT token pair and user details)
- **Notes**:
  - Login is rate-limited to **5 attempts per 60 seconds** per email or username.
  - Exceeding the limit returns `429 Too Many Requests`.

### 3. Refresh Access Token
- **HTTP Method**: `POST`
- **Path**: `/api/v1/auth/refresh`
- **Request Body**:
  ```json
  {
    "refreshToken": "eyJhbGciOiJSUzI1NiJ9..."
  }
  ```
- **Response**: `200 OK` (returns a **new** access and refresh token pair)
- **Notes**:
  - Refresh tokens are single-use: a successful refresh increments the user's `refreshTokenVersion` and invalidates the submitted refresh token.
  - If the refresh token's version does not match the user's current version, the request is rejected.

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
- **Notes**:
  - The access token is added to a Redis blacklist with a TTL equal to its remaining lifetime.
  - Blacklisted tokens are rejected by resource-server checks.

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
    "newPassword": "NewSecretPass123!"
  }
  ```
- **Response**: `200 OK`
- **Notes**:
  - Resets the password with the new value and invalidates the recovery token.
  - Also increments the user's `refreshTokenVersion`, revoking all outstanding refresh tokens as a security measure.

### 7. JSON Web Key Set (JWKS)
- **HTTP Method**: `GET`
- **Paths**: `/.well-known/jwks.json` (e.g. `http://localhost:8080/.well-known/jwks.json`), also referenced as `/api/v1/auth/.well-known/jwks.json` depending on routing
- **Response**: `200 OK` — RSA public key set (`kty=RSA`, `use=sig`, `alg=RS256`, `kid=auth-key-1`)
- **Notes**:
  - Other microservices call this endpoint to fetch the public key and verify access tokens issued by `auth`.
  - Tokens are **not** signed with a shared `jwt.secret`; they are signed with an RSA private key and verified with this JWKS.

---

## LocalStack Messaging Resources

When `docker compose -f auth/docker-compose.yml up -d` runs, `localstack/init-aws.sh` creates the following resources used by this service:

| Resource | Name | Type | Purpose |
|---|---|---|---|
| SNS Topic | `user-events` | Standard | Publish user registration / lifecycle events |
| SNS Topic | `match-events.fifo` | FIFO | Source topic for match events consumed by `auth-match-events-queue.fifo` |
| SQS Queue | `auth-match-events-queue.fifo` | FIFO | Consume match events relevant to auth |

The configured ARNs/URLs in `application.yml` are:

```yaml
aws:
  sns:
    user-events-topic-arn: arn:aws:sns:us-east-1:000000000000:user-events
    match-events-topic-arn: arn:aws:sns:us-east-1:000000000000:match-events.fifo
  sqs:
    auth-match-events-queue-url: http://localhost:4566/000000000000/auth-match-events-queue.fifo
```

Trace context is propagated through SNS message attributes (`traceId`) and restored from SQS messages when processing match events.

---

## Security & Token Notes

- **JWT Signing**: Tokens are signed with **RSA `RS256`** using an RSA key pair. The public key is exposed via the JWKS endpoint. No `jwt.secret` property is used for signing.
- **Refresh Token Rotation**: Calling `/api/v1/auth/refresh` returns a new token pair and bumps the stored `refreshTokenVersion`; the old refresh token can no longer be used.
- **Refresh Token Invalidation on Password Change**: A successful `/api/v1/auth/reset-password` (and any future password-change flow) increments `refreshTokenVersion`, revoking all existing refresh tokens.
- **Access Token Blacklist**: On logout, the access token is stored in Redis with its remaining TTL, preventing reuse until it naturally expires.
- **Registration Race Handling**: `UserRegistrationService` is `@Transactional`; if a duplicate `username` or `email` slips past the pre-check due to a race, the resulting `DataIntegrityViolationException` is converted to `UserAlreadyExistsException`, which the `GlobalExceptionHandler` maps to `409 Conflict`.
- **Login Rate Limiting**: `RateLimiterService` uses Redis to track login attempts per identity. Limit: **5 attempts per 60 seconds**. If Redis is unavailable, an in-memory fallback applies the same limit.

---

## CI/CD Pipeline

Continuous Integration is automated via GitHub Actions:

- [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) — runs on push and pull requests.
- [`.github/workflows/build.yml`](../.github/workflows/build.yml) — runs SonarCloud analysis.

Both workflows build and verify the `auth` service **first**, then the `social` service:

1. Start PostgreSQL, Redis, and MailHog service containers.
2. Run `mvn clean verify` in `./auth`.
3. Run `mvn clean verify` in `./social`.
4. Fail the build if line coverage drops below **80%**.
5. Upload JaCoCo coverage report artifacts.
