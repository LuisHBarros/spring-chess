# Chess Project

This repository contains the microservices and infrastructure for the Chess platform.

## 📦 Services

- [**Authentication Microservice (`auth`)**](./auth/README.md): Spring Boot 3 DDD Auth service with PostgreSQL, Redis, MailHog, JWT, and GitHub Actions CI.
- [**Observability Service (`observation`)**](./observation/README.md): Full telemetry LGTM stack with Prometheus, Grafana, Grafana Tempo (MinIO S3 storage), Loki, and Promtail.

---

## ⚡ Quick Start

### Start Docker Infrastructure

```bash
# Start Auth Infrastructure (PostgreSQL, Redis, MailHog)
docker compose -f auth/docker-compose.yml up -d

# Start Observability Infrastructure (Prometheus, Grafana, Tempo, Loki, MinIO)
docker compose -f observation/docker-compose.yml up -d
```

### Run Tests and Code Coverage Verification

```bash
cd auth
mvn clean verify
```

