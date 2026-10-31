package io.auditplatform.auditor

import reactor.core.publisher.Mono

class Auditor(
    private val properties: AuditorProperties,
    private val publisher: AuditPublisher,
    private val snapshotMapper: JsonSnapshotMapper,
    private val diffEngine: JsonDiffEngine
) {

    fun recordCreate(
        entityType: String,
        entityId: String,
        after: Any,
        tenantId: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): Mono<AuditReceipt> = record(
        operation = AuditOperation.CREATE,
        entityType = entityType,
        entityId = entityId,
        before = null,
        after = after,
        tenantId = tenantId,
        metadata = metadata
    )

    fun recordUpdate(
        entityType: String,
        entityId: String,
        before: Any,
        after: Any,
        tenantId: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): Mono<AuditReceipt> = record(
        operation = AuditOperation.UPDATE,
        entityType = entityType,
        entityId = entityId,
        before = before,
        after = after,
        tenantId = tenantId,
        metadata = metadata
    )

    fun recordDelete(
        entityType: String,
        entityId: String,
        before: Any,
        tenantId: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): Mono<AuditReceipt> = record(
        operation = AuditOperation.DELETE,
        entityType = entityType,
        entityId = entityId,
        before = before,
        after = null,
        tenantId = tenantId,
        metadata = metadata
    )

    fun recordRead(
        entityType: String,
        entityId: String,
        tenantId: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): Mono<AuditReceipt> = record(
        operation = AuditOperation.READ,
        entityType = entityType,
        entityId = entityId,
        before = null,
        after = null,
        tenantId = tenantId,
        metadata = metadata
    )

    fun record(
        operation: AuditOperation,
        entityType: String,
        entityId: String,
        before: Any?,
        after: Any?,
        tenantId: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): Mono<AuditReceipt> = Mono.deferContextual { reactorContext ->
        val executionContext = reactorContext.getOrDefault(
            AuditContextWebFilter.CONTEXT_KEY,
            AuditExecutionContext()
        ) ?: AuditExecutionContext()

        val resolvedTenantId = tenantId ?: executionContext.tenantId
            ?: return@deferContextual Mono.error(IllegalStateException("tenantId is required for audit events"))

        val beforeNode = snapshotMapper.toRedactedNode(before)
        val afterNode = snapshotMapper.toRedactedNode(after)

        val event = AuditEvent(
            tenantId = resolvedTenantId,
            operation = operation,
            entity = AuditEntity(entityType, entityId),
            actor = executionContext.actor,
            source = AuditSource(
                service = properties.serviceName,
                environment = properties.environment,
                instanceId = properties.instanceId
            ),
            request = executionContext.request,
            before = snapshotMapper.toMap(beforeNode),
            after = snapshotMapper.toMap(afterNode),
            changes = diffEngine.diff(beforeNode, afterNode),
            metadata = metadata
        )

        publisher.publish(event)
    }
}
