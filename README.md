# Chess Project

This repository contains the microservices and infrastructure for the Chess platform.

## 📦 Services

- [**Authentication Microservice (`auth`)**](./auth/README.md): Spring Boot 3 DDD Auth service with LocalStack RDS (PostgreSQL), AWS SNS/SQS messaging, Redis, MailHog, JWT, and GitHub Actions CI.
- [**Social Microservice (`social`)**](./social/README.md): Spring Boot 3 DDD Social Interactions & Guilds service with LocalStack RDS (PostgreSQL) and AWS SNS/SQS messaging.
- [**Observability Service (`observation`)**](./observation/README.md): Full telemetry LGTM stack with Prometheus, Grafana, Grafana Tempo (LocalStack S3 backend), Loki, and Promtail.

---

## ⚡ Quick Start

### Start Docker Infrastructure

```bash
# Start AWS & Microservice Infrastructure (LocalStack for SQS/SNS/S3/RDS, Redis, MailHog)
docker compose -f auth/docker-compose.yml up -d
docker compose -f social/docker-compose.yml up -d

# Start Observability Infrastructure (Prometheus, Grafana, Tempo, Loki)
docker compose -f observation/docker-compose.yml up -d
```

### Run Tests and Code Coverage Verification

```bash
cd auth
mvn clean verify
```
