# Development Guide

This document explains how to build, run, and test the `spring-chess` microservices locally.

## Prerequisites

- Java 17 JDK
- Maven 3.8+
- Docker Engine and Docker Compose (v2 recommended)
- An IDE that supports Spring Boot / Java (optional, e.g. IntelliJ IDEA, Eclipse, VS Code)

The repository is organized as a Maven monorepo with a separate `pom.xml` per service. There is no root aggregator POM, so build and test commands must be run from each service directory, or with `mvn -f <service>/pom.xml ...`.

## Repository layout

```
.
├── .env                          # Local secrets; gitignored
├── .github/workflows/
│   ├── ci.yml                    # CI: build + test auth & social
│   └── build.yml                 # SonarCloud analysis for auth & social
├── auth/                         # Port 8080
├── social/                       # Port 8081
├── chat/                         # Port 8082
├── game/                         # Port 8083
├── localstack/
│   └── init-aws.sh               # LocalStack AWS resource init
├── observation/                  # Prometheus, Grafana, Tempo, Loki, Promtail
└── DEVELOPMENT.md
```

## Environment variables

Create a `.env` file in the repository root. It is gitignored (`.gitignore`, line 28) and must never be committed.

```properties
AUTH_DB_PASSWORD=auth_pass
SOCIAL_DB_PASSWORD=social_pass
CHAT_DB_PASSWORD=chat_pass
GAME_DB_PASSWORD=game_pass
```

Each service loads this file through:

```yaml
spring:
  config:
    import: optional:file:../.env[.properties]
```

The `auth`, `social`, and `game` `docker-compose.yml` files also load `.env` with:

```yaml
env_file:
  - ../.env
```

The `localstack/init-aws.sh` script uses these values to create the RDS PostgreSQL instances and other AWS resources. If you need to rotate secrets, change them in `.env` and re-run the init script.

## Service ports

| Service | Application port | Database (host → container) | Extra local infrastructure | Health / Docs |
|---|---|---|---|---|
| `auth` | 8080 | 5432 → 5432 (`auth_db`) | LocalStack gateway 4566, Redis 6379, MailHog 1025 / 8025 | `/actuator/health`, `/swagger-ui.html`, `/v3/api-docs` |
| `social` | 8081 | 5433 → 5432 (`social_db`) | LocalStack gateway 4566 | same |
| `chat` | 8082 | 5434 → 5432 (`chat_db`) | LocalStack gateway 4566 (start with another stack) | same |
| `game` | 8083 | 5435 → 5432 (`game_db`) | LocalStack gateway 4566 | same |

All services with SpringDoc expose:

- Swagger UI: `http://localhost:<service-port>/swagger-ui.html`
- OpenAPI docs: `http://localhost:<service-port>/v3/api-docs`

Only `auth` and `game` currently include `spring-boot-starter-actuator`, so the following endpoints are only available for those two services:

- Health: `GET /actuator/health`
- Prometheus metrics: `GET /actuator/prometheus`

Service ports are `8080` (`auth`), `8081` (`social`), `8082` (`chat`), and `8083` (`game`).

## Local infrastructure

### 1. Observability stack

Start the Prometheus / Grafana / Tempo / Loki stack from the repository root:

```bash
docker compose -f observation/docker-compose.yml up -d
```

| Component | URL / Port |
|---|---|
| Grafana | http://localhost:3000 (admin / admin) |
| Prometheus | http://localhost:9090 |
| Tempo | http://localhost:3200, OTLP gRPC 4317, OTLP HTTP 4318 |
| Loki | http://localhost:3100 |

The `auth` service is pre-configured in `observation/prometheus/prometheus.yml` on `host.docker.internal:8080`. Add the other services to the scrape config as needed. Tempo is configured to store traces in LocalStack S3 (`tempo-traces` bucket on `host.docker.internal:4566`).

### 2. AWS / PostgreSQL infrastructure

Each service has its own `docker-compose.yml` that brings up the infrastructure it needs.

```bash
# auth (LocalStack + RDS + Redis + MailHog)
docker compose -f auth/docker-compose.yml up -d

# social (LocalStack + RDS)
docker compose -f social/docker-compose.yml up -d

# chat (dedicated PostgreSQL container)
docker compose -f chat/docker-compose.yml up -d

# game (LocalStack + RDS)
docker compose -f game/docker-compose.yml up -d
```

The `auth`, `social`, and `game` `docker-compose.yml` files each define a LocalStack container named `chess-localstack` that binds to port `4566`. Docker will prevent two of them from running at the same time because the container name and port are identical. For day-to-day development on a single service, start the compose file for the service you are currently changing. If you need to run multiple microservices simultaneously, use a single LocalStack instance and override each service's datasource URL to the correct exposed PostgreSQL port (see [Running a service](#running-a-service)).

### 3. Provision AWS resources

The LocalStack init script is at `localstack/init-aws.sh` and is mounted into the LocalStack container at `/etc/localstack/init/ready.d/init-aws.sh`. LocalStack normally executes it automatically when the container reaches the ready state. To re-run it manually, or to verify it ran:

```bash
docker compose -f <service>/docker-compose.yml exec localstack /etc/localstack/init/ready.d/init-aws.sh
```

The script creates:

- S3 bucket `tempo-traces`
- SNS topics: `user-events`, `match-events.fifo`, `social-events`, `chat-events`
- SQS queues and dead-letter queues for `social`, `chat`, `analytics`, and `auth`
- Subscriptions between the SNS topics and SQS queues
- RDS PostgreSQL instances for `auth_db`, `social_db`, `chat_db`, and `game_db`
- DynamoDB table `game-history`

The script uses `|| true` for RDS and DynamoDB calls, so repeated runs are safe.

## Running a service

Each service is a standalone Spring Boot application. Run it from the service directory:

```bash
cd auth
mvn spring-boot:run
```

For `chat`, the default datasource already points to `localhost:5434`, matching `chat/docker-compose.yml`. For `social` and `game`, the default `application.yml` points to `localhost:5432`, which matches the `auth` LocalStack. If you started the `social` or `game` compose files (which expose their databases on `5433` or `5435`), override the datasource at startup:

```bash
cd social
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://localhost:5433/social_db"
```

or via environment variable:

```bash
cd game
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5435/game_db
mvn spring-boot:run
```

Alternatively, start the application inside the same Docker network and use the LocalStack service hostname with container port `5432`.

## Testing

Run the full test suite and JaCoCo coverage from inside the service directory:

```bash
cd <service>
mvn clean verify
```

- `auth` and `social`: use an in-memory **H2** database for tests (`src/test/resources/application.yml`). No Docker is required for these test suites.
- `chat` and `game`: use the main `application.yml` and therefore a real PostgreSQL database with `ddl-auto: update` when run locally. Start the appropriate `docker-compose.yml` before running `mvn clean verify`.

Example:

```bash
docker compose -f chat/docker-compose.yml up -d
cd chat
mvn clean verify
```

If a `chat` or `game` test leaves stale data, drop and recreate the test database, or set `ddl-auto: create-drop` for a fresh test run (not recommended for shared dev databases).

## Continuous Integration

The repository has two GitHub Actions workflows in `.github/workflows/`:

- `ci.yml`: triggers on pushes and PRs to `main`, `master`, `development`, `production`, and `prod`. It provisions Postgres (`5432`), Redis (`6379`), and MailHog (`1025` / `8025`) as service containers and runs `mvn clean verify` for `auth` and `social` only.
- `build.yml`: runs SonarCloud analysis for `auth` and `social` on `main` and `development` branches, falling back to a plain `mvn -B verify` when `SONAR_TOKEN` is not configured.

`chat` and `game` are not currently exercised in CI.

## Troubleshooting

### `mvn` is not recognized
Ensure Maven 3.8+ is installed and `mvn` is on your `PATH`. Verify with `mvn -v` and that `JAVA_HOME` points to a Java 17 JDK.

### Database password is not set
Create the root `.env` file as shown in [Environment variables](#environment-variables). The `spring.config.import` is `optional:file:...`, so the application will start without the file, but it will fail when it tries to connect to the database with an unresolved placeholder.

### `.env` changes not reflected
Spring Boot may cache external property files. Stop the application and restart it, or pass the values directly as environment variables or `-D` arguments.

### LocalStack resources are missing
If you see errors such as "topic does not exist" or "queue does not exist", the init script has not run or failed. Check the LocalStack container:

```bash
docker compose -f <service>/docker-compose.yml logs localstack
```

Then run the init script manually:

```bash
docker compose -f <service>/docker-compose.yml exec localstack /etc/localstack/init/ready.d/init-aws.sh
```

Verify inside the container:

```bash
docker compose -f <service>/docker-compose.yml exec localstack awslocal s3 ls
docker compose -f <service>/docker-compose.yml exec localstack awslocal rds describe-db-instances
```

### Port conflicts
Many ports are bound to `localhost`:

- `8080–8083` (Spring Boot applications)
- `5432–5435` (PostgreSQL / LocalStack RDS)
- `4566` (LocalStack gateway)
- `6379` (Redis)
- `1025` / `8025` (MailHog)
- `9090` (Prometheus)
- `3000` (Grafana)
- `3100` (Loki)
- `3200` / `4317` / `4318` (Tempo)

If a port is already in use, either stop the conflicting process or adjust the host port mapping in the relevant `docker-compose.yml`.

### Cannot connect to PostgreSQL for `social` or `game`
The default `application.yml` for these services points to `localhost:5432`. When you start `social/docker-compose.yml` or `game/docker-compose.yml`, the database is exposed on a different host port (`5433` or `5435`). Override the URL:

```bash
cd social
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://localhost:5433/social_db"
```

For `game`:

```bash
cd game
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://localhost:5435/game_db"
```

### Chat or Game tests fail with a database connection error
These services do not use H2 for tests. Start their PostgreSQL / LocalStack containers first:

```bash
docker compose -f chat/docker-compose.yml up -d
# or
docker compose -f game/docker-compose.yml up -d
```

Then run `mvn clean verify` and override the datasource URL if the exposed host port differs from the default.

### Multiple LocalStack instances fail to start
Only one LocalStack container named `chess-localstack` can run because they all bind to `4566`. Run a single compose file, or remove the existing container with `docker compose -f <service>/docker-compose.yml down` before starting another.
