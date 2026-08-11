# Chess Project

This repository contains the microservices for the Chess platform.

## 📦 Services

- [**Authentication Microservice (`auth`)**](./auth/README.md): Spring Boot 3 DDD Auth service with PostgreSQL, Redis, MailHog, JWT, and GitHub Actions CI.

---

## ⚡ Quick Start

### Start Docker Infrastructure

```bash
docker compose -f auth/docker-compose.yml up -d
```

### Run Tests and Code Coverage Verification

```bash
cd auth
mvn clean verify
```
