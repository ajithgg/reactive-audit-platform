package io.auditplatform.userplatform.outbox

import io.auditplatform.auditor.outbox.AuditOutboxEntry
import io.auditplatform.auditor.outbox.AuditOutboxRepository
import io.auditplatform.auditor.outbox.AuditOutboxStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Relays pending outbox rows to Kafka. Ordering is best-effort per tenant via message key.
 */
@Component
@ConditionalOnProperty(prefix = "auditor", name = ["publisher"], havingValue = "outbox")
@ConditionalOnProperty(prefix = "auditor.outbox.relay", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class AuditOutboxRelay(
    private val repository: AuditOutboxRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val properties: AuditOutboxRelayProperties,
    @Value("\${auditor.kafka.topic:audit-events}") private val topic: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelayString = "\${auditor.outbox.relay.fixed-delay-ms:2000}")
    fun relay() {
        repository.findByStatusOrderByCreatedAtAsc(AuditOutboxStatus.PENDING.name, properties.batchSize)
            .flatMap { entry -> publish(entry) }
            .then()
            .subscribe(
                {},
                { error -> log.error("Outbox relay iteration failed", error) }
            )
    }

    private fun publish(entry: AuditOutboxEntry): Mono<AuditOutboxEntry> {
        return Mono.fromCallable {
            kafkaTemplate.send(topic, entry.tenantId, entry.payloadJson).get()
        }.flatMap {
            repository.save(
                entry.copy(
                    status = AuditOutboxStatus.SENT.name,
                    sentAt = Instant.now(),
                    lastError = null
                )
            )
        }.onErrorResume { error ->
            val attempts = entry.attempts + 1
            val failed = attempts >= properties.maxAttempts
            log.warn(
                "Failed to relay audit event {} (attempt {}): {}",
                entry.eventId,
                attempts,
                error.message
            )
            repository.save(
                entry.copy(
                    status = if (failed) AuditOutboxStatus.FAILED.name else AuditOutboxStatus.PENDING.name,
                    attempts = attempts,
                    lastError = error.message?.take(2000)
                )
            )
        }
    }
}