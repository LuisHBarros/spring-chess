# 🔭 Observability Service (`observation`)

This directory contains the complete Observability Stack for the **Spring Chess** microservices platform.

It combines **Prometheus**, **Grafana**, **Grafana Tempo**, **Grafana Loki**, **Promtail**, and **MinIO** to provide a unified telemetry solution for **Metrics, Logs, and Traces** (LGTM stack) with S3-compatible backend storage.

---

## 🏗️ Architecture & Component Overview

```
                          ┌───────────────────────────┐
                          │   Grafana (Port 3000)     │
                          └─────────────┬─────────────┘
                                        │
             ┌──────────────────────────┼──────────────────────────┐
             ▼                          ▼                          ▼
  ┌─────────────────────┐    ┌─────────────────────┐    ┌─────────────────────┐
  │     Prometheus      │    │    Grafana Tempo    │    │    Grafana Loki     │
  │     (Port 9090)     │    │     (Port 3200)     │    │     (Port 3100)     │
  └──────────▲──────────┘    └──────────▲──────────┘    └──────────▲──────────┘
             │                          │                          │
      Metrics Scrape             OTLP Traces (4317/4318)      Log Ingestion (3100)
             │                          │                          │
  ┌──────────┴──────────┐    ┌──────────┴──────────┐    ┌──────────┴──────────┐
  │ Microservices (auth)│    │ Microservices (auth)│    │ Promtail / Docker   │
  └─────────────────────┘    └─────────────────────┘    └─────────────────────┘
                                        │
                               S3 Object Storage
                                        │
                             ┌──────────▼──────────┐
                             │   MinIO / AWS S3    │
                             │ (Ports 9000 / 9001) │
                             └─────────────────────┘
```

### Components

| Service | Port | Description | Credentials / Details |
|---|---|---|---|
| **Grafana** | `3000` | Telemetry Dashboards & Visualization | `admin` / `admin` |
| **Prometheus** | `9090` | Time-series Metrics Scraper & Database | Scrapes `/actuator/prometheus` |
| **Grafana Tempo** | `3200` (UI/API)<br>`4317` (gRPC)<br>`4318` (HTTP) | Distributed Tracing Backend | Receives OTLP traces; stores blocks in S3/MinIO |
| **Grafana Loki** | `3100` | Log Aggregation System | TSDB storage engine for application logs |
| **Promtail** | `9080` | Docker Container Log Collector | Streams logs from Docker engine to Loki |
| **MinIO (S3)** | `9000` (API)<br>`9001` (Console) | S3-Compatible Object Storage for Traces | `minioadmin` / `minioadmin`<br>Bucket: `tempo-traces` |

---

## ⚡ Quick Start

### 1. Start the Observability Stack

From the root repository or this directory, execute:

```bash
docker compose -f observation/docker-compose.yml up -d
```

### 2. Verify Services

- **Grafana**: [http://localhost:3000](http://localhost:3000) (Login: `admin` / `admin`)
- **Prometheus**: [http://localhost:9090](http://localhost:9090)
- **Tempo**: [http://localhost:3200](http://localhost:3200)
- **MinIO Console**: [http://localhost:9001](http://localhost:9001) (Login: `minioadmin` / `minioadmin`)

---

## 🪣 Trace Storage (MinIO & S3 Configuration)

Grafana Tempo stores trace blocks in S3 object storage.

### Local Development (MinIO)
In local development, Tempo automatically streams traces to the local **MinIO** service into the `tempo-traces` bucket:

```yaml
storage:
  trace:
    backend: s3
    s3:
      bucket: tempo-traces
      endpoint: minio:9000
      access_key: minioadmin
      secret_key: minioadmin
      insecure: true
      s3forcepathstyle: true
```

### Production Deployment (AWS S3)
To switch to AWS S3 in production, update `observation/tempo/tempo.yaml` or override environment variables:

```yaml
storage:
  trace:
    backend: s3
    s3:
      bucket: <your-production-s3-bucket-name>
      endpoint: s3.<your-region>.amazonaws.com
      # Credentials can be loaded via IAM Roles or environment variables
```

---

## 🔗 Spring Boot Microservice Integration Guide

To send **metrics**, **traces**, and **logs** from a Spring Boot service (e.g. `auth`) to this observability stack:

### 1. Add Dependencies (`pom.xml`)

```xml
<!-- Prometheus Metrics -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<!-- Tracing (Micrometer Tracing + OTLP exporter to Tempo) -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>

<!-- JSON Logging for Loki / Promtail -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

### 2. Configure `application.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus
  metrics:
    tags:
      application: ${spring.application.name}
  tracing:
    sampling:
      probability: 1.0 # 100% sample rate for development
  otlp:
    tracing:
      endpoint: http://localhost:4318/v1/traces

logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

---

## 📊 Pre-provisioned Grafana Setup

When Grafana starts up, it automatically provisions:
1. **Datasources**:
   - **Prometheus** (Default metrics engine)
   - **Tempo** (Trace search with node graphs and trace-to-logs link to Loki)
   - **Loki** (Log search with derived regex field linking `traceId` directly to Tempo traces)
2. **Dashboards**:
   - **Spring Chess Observability Overview** (`spring-chess-overview`)
