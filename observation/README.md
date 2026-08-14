# Observability Stack (`observation`)

This directory contains a **local-only** telemetry stack for the Spring Chess microservices.

It implements an **LGTM-style** architecture:

- **Prometheus** — metrics collection
- **Grafana** — dashboards and visualization
- **Tempo** — distributed tracing
- **Loki** — log aggregation
- **Promtail** — log collection from Docker containers

> This setup is intended for **local development only**. It is not a production observability deployment.

---

## Component Overview

| Service | Container Name | Host Port(s) | Purpose |
|---|---|---|---|
| **Prometheus** | `chess-prometheus` | `9090` | Scrapes and stores time-series metrics. |
| **Grafana** | `chess-grafana` | `3000` | Visualization UI with provisioned datasources and dashboards. |
| **Grafana Tempo** | `chess-tempo` | `3200` (UI/API), `4317` (OTLP gRPC), `4318` (OTLP HTTP) | Receives and stores distributed traces. |
| **Grafana Loki** | `chess-loki` | `3100` | Log aggregation backend. |
| **Promtail** | `chess-promtail` | `9080` (internal HTTP server) | Discovers Docker containers and pushes logs to Loki. |

### Architecture Notes

- **No MinIO** is included in this stack.
- **Grafana Tempo stores trace blocks in the existing LocalStack S3** (`host.docker.internal:4566`, bucket `tempo-traces`) as configured in `observation/tempo/tempo.yaml`.
- **Grafana credentials are hard-coded for local use:** `admin` / `admin`.
- Prometheus is configured to scrape itself, Tempo, Loki, and the `auth-service` at `host.docker.internal:8080` (see `observation/prometheus/prometheus.yml`).
- Grafana datasources are pre-provisioned (`observation/grafana/provisioning/datasources/datasources.yml`) for Prometheus, Tempo, and Loki.
- Dashboards are pre-provisioned from `observation/grafana/dashboards/` (`observation/grafana/provisioning/dashboards/dashboards.yml`).

---

## Quick Start

Start the stack with:

```bash
docker compose -f observation/docker-compose.yml up -d
```

Wait for the healthy state, then open the UIs:

| Service | URL |
|---|---|
| **Grafana** | http://localhost:3000 |
| **Prometheus** | http://localhost:9090 |
| **Tempo** | http://localhost:3200 |
| **Loki** | http://localhost:3100 |

### Grafana Login

- **Username:** `admin`
- **Password:** `admin`

### Prometheus Targets

Open http://localhost:9090/targets to verify that the configured scrape endpoints are reachable.

---

## Pre-provisioned Grafana Configuration

When Grafana starts, it automatically loads:

1. **Datasources**
   - **Prometheus** (default)
   - **Tempo** (trace search with node graphs and trace-to-logs link to Loki)
   - **Loki** (log search with a derived `traceId` field linked to Tempo)

2. **Dashboards**
   - `spring-chess-overview` — high-level platform view
   - `auth-http` — Auth service HTTP/API metrics
   - `auth-jvm` — Auth service JVM metrics

---

## Tempo S3 Storage

Tempo is configured to use the **LocalStack S3** endpoint (not MinIO):

```yaml
storage:
  trace:
    backend: s3
    s3:
      bucket: tempo-traces
      endpoint: host.docker.internal:4566
      access_key: test
      secret_key: test
      insecure: true
      forcepathstyle: true
```

Make sure LocalStack is running and the `tempo-traces` S3 bucket exists. This bucket is created by `localstack/init-aws.sh`.

---

## Important Notes

- This is **local telemetry only** and should not be used as-is in production.
- The local-only `admin`/`admin` credentials are set via `GF_SECURITY_ADMIN_USER` and `GF_SECURITY_ADMIN_PASSWORD` in `observation/docker-compose.yml`.
- The Prometheus `auth-service` job points to `host.docker.internal:8080`; adjust per service or target as needed for your local runs.
