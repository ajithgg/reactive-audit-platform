package io.auditplatform.auditor.outbox

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.UUID

interface AuditOutboxRepository : ReactiveCrudRepository<AuditOutboxEntry, UUID> {

    @Query(
        """
        SELECT * FROM audit_outbox
        WHERE status = :status
        ORDER BY created_at ASC
        LIMIT :limit
        """
    )
    fun findByStatusOrderByCreatedAtAsc(status: String, limit: Int): Flux<AuditOutboxEntry>
}