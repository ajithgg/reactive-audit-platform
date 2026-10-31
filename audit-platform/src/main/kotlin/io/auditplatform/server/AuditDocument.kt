package io.auditplatform.server

import io.auditplatform.auditor.AuditChangeOperation
import io.auditplatform.auditor.AuditEvent
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.Instant

@Document(indexName = "audit-events-v1", createIndex = true)
data class AuditDocument(
    @Id
    val eventId: String,

    @Field(type = FieldType.Keyword)
    val schemaVersion: String,

    @Field(type = FieldType.Date, format = [DateFormat.date_time])
    val occurredAt: Instant,

    @Field(type = FieldType.Keyword)
    val tenantId: String,

    @Field(type = FieldType.Keyword)
    val operation: String,

    @Field(type = FieldType.Keyword)
    val entityType: String,

    @Field(type = FieldType.Keyword)
    val entityId: String,

    @Field(type = FieldType.Keyword)
    val actorId: String? = null,

    @Field(type = FieldType.Keyword)
    val actorType: String? = null,

    @Field(type = FieldType.Keyword)
    val sourceService: String,

    @Field(type = FieldType.Keyword)
    val sourceEnvironment: String,

    @Field(type = FieldType.Keyword)
    val sourceInstanceId: String? = null,

    @Field(type = FieldType.Keyword)
    val requestMethod: String? = null,

    @Field(type = FieldType.Keyword)
    val requestPath: String? = null,

    @Field(type = FieldType.Keyword)
    val correlationId: String? = null,

    @Field(type = FieldType.Keyword)
    val traceId: String? = null,

    @Field(type = FieldType.Keyword)
    val requestId: String? = null,

    @Field(type = FieldType.Flattened)
    val before: Map<String, Any?>? = null,

    @Field(type = FieldType.Flattened)
    val after: Map<String, Any?>? = null,

    /** Only fields that changed, with their previous values. */
    @Field(type = FieldType.Flattened)
    val changesBefore: Map<String, Any?> = emptyMap(),

    /** Only fields that changed, with their new values. */
    @Field(type = FieldType.Flattened)
    val changesAfter: Map<String, Any?> = emptyMap(),

    @Field(type = FieldType.Flattened)
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        fun from(event: AuditEvent): AuditDocument {
            val (changesBefore, changesAfter) = toChangeMaps(event.changes)
            return AuditDocument(
                eventId = event.eventId,
                schemaVersion = event.schemaVersion,
                occurredAt = event.occurredAt,
                tenantId = event.tenantId,
                operation = event.operation.name,
                entityType = event.entity.type,
                entityId = event.entity.id,
                actorId = event.actor?.id,
                actorType = event.actor?.type,
                sourceService = event.source.service,
                sourceEnvironment = event.source.environment,
                sourceInstanceId = event.source.instanceId,
                requestMethod = event.request?.method,
                requestPath = event.request?.path,
                correlationId = event.request?.correlationId,
                traceId = event.request?.traceId,
                requestId = event.request?.requestId,
                before = event.before,
                after = event.after,
                changesBefore = changesBefore,
                changesAfter = changesAfter,
                metadata = event.metadata
            )
        }

        /**
         * Converts JSON-pointer style diffs into two flat key/value maps of changed fields only.
         * Paths like `/displayName` or `/attributes/department` become `displayName` / `attributes.department`.
         * Root-object diffs (`/`) are skipped — full snapshots already live in `before`/`after`.
         */
        internal fun toChangeMaps(changes: List<io.auditplatform.auditor.AuditChange>): Pair<Map<String, Any?>, Map<String, Any?>> {
            val before = linkedMapOf<String, Any?>()
            val after = linkedMapOf<String, Any?>()
            for (change in changes) {
                val key = pathToFieldKey(change.path) ?: continue
                when (change.operation) {
                    AuditChangeOperation.ADD -> after[key] = change.newValue
                    AuditChangeOperation.REMOVE -> before[key] = change.oldValue
                    AuditChangeOperation.REPLACE -> {
                        before[key] = change.oldValue
                        after[key] = change.newValue
                    }
                }
            }
            return before to after
        }

        internal fun pathToFieldKey(path: String): String? {
            if (path.isBlank() || path == "/") return null
            return path.trimStart('/').replace('/', '.')
        }
    }
}
