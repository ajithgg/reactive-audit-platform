# Architecture Decisions

## ADR-001: PostgreSQL as the user platform source of truth

**Decision:** Use PostgreSQL with R2DBC.

**Why:**
- tenant + external user uniqueness can be enforced in the database;
- user updates are structured and transactional;
- identity/tenant data benefits from relational constraints more than document flexibility;
- R2DBC keeps the request path non-blocking in the Spring WebFlux service;
- JSON-like extension attributes can still be supported while core identity fields remain strongly modeled.

MongoDB would be reasonable if user records were highly heterogeneous and relational integrity were unimportant, but that is not the dominant requirement here.

## ADR-002: Elasticsearch is the audit search store, not the user system of record

The audit service stores immutable-style event documents in the `audit-events-v1` index. Search keys are flattened at the top level (`tenantId`, `entityType`, `entityId`, `actorId`, etc.). Full before/after states use Elasticsearch `flattened` fields to avoid mapping explosion from arbitrary domain payloads.

Changed values are serialized to JSON strings in the Elasticsearch projection so that a field changing between numeric/string/object shapes across different business domains cannot cause mapping conflicts.

## ADR-003: Generic auditor SDK

The SDK knows nothing about users or any other business domain. A caller provides:
- `entityType`
- `entityId`
- operation
- before object
- after object
- optional metadata

The SDK adds tenant, actor, request, source-service and timestamp information.

## ADR-004: Tenant/platform split

`user-tenant` is an edge/facade service. It carries tenant and actor context and calls `user-platform`.

`user-platform` owns user persistence and therefore owns the canonical CRUD audit event. Auditing at the actual mutation boundary avoids double-auditing the same logical change.

## ADR-005: Publisher is replaceable

V1 contains `HttpAuditPublisher` for easy local development. `Auditor` depends only on the `AuditPublisher` interface.

For production-grade completeness, add a transactional outbox adapter. The event contract and business calls stay unchanged.

## Audit semantics

| Operation | Before | After | changesBefore / changesAfter |
|---|---|---|---|
| CREATE | null | full created object | empty (full object is in `after`) |
| READ | null | null | empty; access metadata only |
| UPDATE | full previous object | full resulting object | only changed fields as key/value maps |
| DELETE | full object at deletion time | null | empty (full object is in `before`) |

Arrays are treated atomically in V1. If any element changes, the array path gets one `REPLACE` change. This avoids unstable index-based diffs and can be upgraded later with keyed-array diff strategies.

## Security baseline

Sensitive field names are redacted before the audit event leaves the application. Default redactions include password, accessToken, refreshToken, authorization and secret.

Production should additionally add authentication/authorization for audit ingestion and querying, encryption in transit, retention rules, and an immutable archive if audit records are compliance-relevant.
