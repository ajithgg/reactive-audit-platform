# Production audit path (outbox + Kafka)

This repo supports two audit delivery modes:

| Mode | `auditor.publisher` | Flow |
|------|---------------------|------|
| Local / simple | `http` (default) | user-platform → HTTP → audit-platform → Elasticsearch |
| Production-style | `outbox` | same DB TX → `audit_outbox` → relay → Kafka → audit-platform → Elasticsearch |

## Run infrastructure

```bash
docker compose up -d
```

Includes PostgreSQL, Elasticsearch, Kibana, and **Kafka** on `localhost:9092`.

If PostgreSQL already existed before `audit_outbox` was added, apply the table manually or recreate the volume.

## Run services (prod profile)

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
export PATH="$JAVA_HOME/bin:$PATH"

gradle :audit-platform:bootRun --args='--spring.profiles.active=prod'
gradle :user-platform:bootRun --args='--spring.profiles.active=prod'
gradle :user-tenant:bootRun
```

## Guarantees

- **Outbox**: user row and outbox row commit together (`@Transactional` on mutations).
- **Relay**: background job publishes pending rows to topic `audit-events` (key = `tenantId`).
- **Consumer**: audit-platform ingests from Kafka with idempotent `eventId` dedupe.
- **HTTP ingest** (optional fallback): when `audit.ingestion.api-key` is set, POST requires `X-Audit-Ingest-Key`.

## Env

| Variable | Purpose |
|----------|---------|
| `AUDIT_INGEST_API_KEY` | HTTP ingest secret for audit-platform (prod profile default: `dev-ingest-key`) |

## Verify

1. CRUD via user-tenant or UI.
2. `SELECT status, count(*) FROM audit_outbox GROUP BY status;` — rows move PENDING → SENT.
3. Kibana / audit API — events appear as before.