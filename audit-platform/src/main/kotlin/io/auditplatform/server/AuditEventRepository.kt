package io.auditplatform.server

import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository
import reactor.core.publisher.Flux

interface AuditEventRepository : ReactiveElasticsearchRepository<AuditDocument, String> {
    fun findAllByTenantIdAndEntityTypeAndEntityIdOrderByOccurredAtDesc(
        tenantId: String,
        entityType: String,
        entityId: String
    ): Flux<AuditDocument>

    fun findAllByTenantIdAndActorIdOrderByOccurredAtDesc(
        tenantId: String,
        actorId: String
    ): Flux<AuditDocument>
}
