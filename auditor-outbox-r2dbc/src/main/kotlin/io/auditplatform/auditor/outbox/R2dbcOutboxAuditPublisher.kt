package io.auditplatform.auditor.outbox

import tools.jackson.databind.ObjectMapper
import io.auditplatform.auditor.AuditEvent
import io.auditplatform.auditor.AuditPublisher
import io.auditplatform.auditor.AuditReceipt
import org.slf4j.LoggerFactory
import org.springframework.r2dbc.core.DatabaseClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Instant
import java.util.UUID

/**
 * Persists audit events to the outbox on a dedicated scheduler so inserts are not tied
 * to a leaked reactive transaction from the business write.
 */
class R2dbcOutboxAuditPublisher(
    private val databaseClient: DatabaseClient,
    private val objectMapper: ObjectMapper
) : AuditPublisher {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(event: AuditEvent): Mono<AuditReceipt> =
        Mono.defer {
            val payloadJson = objectMapper.writeValueAsString(event)
            val id = UUID.randomUUID()
            val now = Instant.now()

            databaseClient.sql(
                """
                INSERT INTO audit_outbox (id, event_id, tenant_id, payload_json, status, created_at, attempts)
                VALUES (:id, :eventId, :tenantId, :payloadJson, :status, :createdAt, :attempts)
                """
            )
                .bind("id", id)
                .bind("eventId", event.eventId)
                .bind("tenantId", event.tenantId)
                .bind("payloadJson", payloadJson)
                .bind("status", AuditOutboxStatus.PENDING.name)
                .bind("createdAt", now)
                .bind("attempts", 0)
                .fetch()
                .rowsUpdated()
                .filter { it > 0L }
                .switchIfEmpty(Mono.error(IllegalStateException("Outbox insert affected 0 rows")))
                .doOnSuccess {
                    log.info("Queued audit event {} in outbox for tenant {}", event.eventId, event.tenantId)
                }
                .map {
                    AuditReceipt(
                        eventId = event.eventId,
                        accepted = true,
                        duplicate = false
                    )
                }
        }.subscribeOn(Schedulers.boundedElastic())
}