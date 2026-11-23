# Reactive Audit Platform

A reusable, tenant-aware CRUD audit platform built with Spring Boot, Kotlin, WebFlux, PostgreSQL/R2DBC, and Elasticsearch.

## Modules

- `auditor-module` — reusable audit SDK. Builds generic audit events, computes before/after diffs, redacts configured fields, extracts request/tenant/actor context, and publishes through a pluggable reactive `AuditPublisher`.
- `auditor-outbox-r2dbc` — `R2dbcOutboxAuditPublisher` for transactional outbox writes (`auditor.publisher=outbox`).
- `audit-platform` — central ingestion/search service. Persists audit events to Elasticsearch.
- `user-platform` — sample platform service that persists tenant-scoped users in PostgreSQL using R2DBC and audits CRUD operations with `auditor-module`.
- `user-tenant` — sample tenant-facing facade. Forwards tenant/user/correlation context to `user-platform` using WebClient.
- `user-console` — React UI for user CRUD against `user-tenant` (list, view/edit, create, delete).

## Why PostgreSQL for user-platform?

User identity and tenant ownership need relational guarantees: uniqueness, tenant scoping, referential integrity, predictable updates, and transactional behavior. PostgreSQL is a better system of record for this than MongoDB. Elasticsearch remains the search/visualization store for audit events.

## Local architecture

```text
Client
  |
  v
user-tenant :8081
  |
  | X-Tenant-Id / X-Actor-Id / X-Correlation-Id
  v
user-platform :8082 ---- R2DBC ----> PostgreSQL :5432
  |
  | auditor-module (reactive HTTP publisher)
  v
audit-platform :8083 ---- reactive ----> Elasticsearch :9200
                                      \\-> Kibana :5601
```

## Prerequisites

- Java 21
- Gradle 8.14+ or 9.x
- Docker / Docker Compose

Spring Boot 4.1.2 and Kotlin 2.3.21 are used in this starter. Spring Boot 4 uses Jackson 3 by default.

## Run infrastructure

```bash
docker compose up -d
```

Check Elasticsearch:

```bash
curl http://localhost:9200
```

Kibana: http://localhost:5601

## Run applications

In separate terminals:

```bash
gradle :audit-platform:bootRun
gradle :user-platform:bootRun
gradle :user-tenant:bootRun
```

Frontend console:

```bash
cd user-console
npm install
npm run dev
```

Open http://localhost:5173 — the Vite dev server proxies `/api` to `user-tenant` on `:8081`.

## Headers

All tenant requests should carry:

```text
X-Tenant-Id: tenant-a
X-Actor-Id: ajith
X-Actor-Type: USER
X-Correlation-Id: optional-correlation-id
```

If `X-Correlation-Id` is absent, the tenant facade creates one.

## Example CRUD flow

Create:

```bash
curl -X POST http://localhost:8081/api/v1/users \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Id: tenant-a' \
  -H 'X-Actor-Id: ajith' \
  -d '{
    "externalId": "u-1001",
    "email": "user@example.com",
    "displayName": "Demo User",
    "status": "ACTIVE",
    "attributes": {"department":"engineering"}
  }'
```

Update:

```bash
curl -X PATCH http://localhost:8081/api/v1/users/<USER_ID> \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Id: tenant-a' \
  -H 'X-Actor-Id: ajith' \
  -d '{"displayName":"Updated User","status":"INACTIVE"}'
```

Delete:

```bash
curl -X DELETE http://localhost:8081/api/v1/users/<USER_ID> \
  -H 'X-Tenant-Id: tenant-a' \
  -H 'X-Actor-Id: ajith'
```

Audit history for the user entity:

```bash
curl 'http://localhost:8083/api/v1/audit-events/entities/UserRecord/<USER_ID>?limit=100' \
  -H 'X-Tenant-Id: tenant-a'
```

## Audit model

Every mutation can contain:

- event/schema version
- tenant
- operation: CREATE / UPDATE / DELETE (reads optional via `auditor.audit-reads`)
- entity type and id
- actor
- source service/environment
- request path/method/correlation id/trace id
- full redacted `before` state
- full redacted `after` state
- `changesBefore` / `changesAfter` maps of only the changed field key/value pairs
- metadata

The audit module is intentionally unaware of `UserRecord`. Any serializable object can be audited.

## Production audit path (outbox + Kafka)

Local default remains **HTTP** (`auditor.publisher=http`). For the **transactional outbox** flow, see **[PRODUCTION.md](PRODUCTION.md)**:

```text
@Transactional mutation
  |- business row (PostgreSQL)
  `- audit_outbox row (same commit)
          |
          v
     outbox relay (user-platform)
          |
          v
        Kafka (audit-events)
          |
          v
     audit-platform -> Elasticsearch
```

Business services keep the same `Auditor` calls; switch publisher via config.
