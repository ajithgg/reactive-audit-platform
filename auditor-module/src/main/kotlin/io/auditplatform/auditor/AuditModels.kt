package io.auditplatform.auditor

import java.time.Instant
import java.util.UUID

enum class AuditOperation {
    CREATE,
    READ,
    UPDATE,
    DELETE
}

enum class AuditChangeOperation {
    ADD,
    REMOVE,
    REPLACE
}

data class AuditEntity(
    val type: String,
    val id: String
)

data class AuditActor(
    val id: String,
    val type: String = "USER"
)

data class AuditRequestContext(
    val method: String? = null,
    val path: String? = null,
    val correlationId: String? = null,
    val traceId: String? = null,
    val requestId: String? = null
)

data class AuditSource(
    val service: String,
    val environment: String,
    val instanceId: String? = null
)

data class AuditChange(
    val path: String,
    val operation: AuditChangeOperation,
    val oldValue: Any? = null,
    val newValue: Any? = null
)

data class AuditEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val schemaVersion: String = "1.0",
    val occurredAt: Instant = Instant.now(),
    val tenantId: String,
    val operation: AuditOperation,
    val entity: AuditEntity,
    val actor: AuditActor? = null,
    val source: AuditSource,
    val request: AuditRequestContext? = null,
    val before: Map<String, Any?>? = null,
    val after: Map<String, Any?>? = null,
    val changes: List<AuditChange> = emptyList(),
    val metadata: Map<String, String> = emptyMap()
)

data class AuditReceipt(
    val eventId: String,
    val accepted: Boolean,
    val duplicate: Boolean = false,
    val index: String? = null
)

data class AuditExecutionContext(
    val tenantId: String? = null,
    val actor: AuditActor? = null,
    val request: AuditRequestContext? = null
)
