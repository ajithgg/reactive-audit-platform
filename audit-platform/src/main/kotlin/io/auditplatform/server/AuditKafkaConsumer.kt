package io.auditplatform.server

import tools.jackson.databind.ObjectMapper
import io.auditplatform.auditor.AuditEvent
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    prefix = "audit.kafka.consumer",
    name = ["enabled"],
    havingValue = "true"
)
class AuditKafkaConsumer(
    private val ingestionService: AuditIngestionService,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${audit.kafka.topic:audit-events}"],
        groupId = "\${audit.kafka.consumer.group-id:audit-platform}"
    )
    fun consume(payload: String) {
        try {
            val event = objectMapper.readValue(payload, AuditEvent::class.java)
            ingestionService.ingest(event)
                .doOnSuccess { receipt ->
                    if (receipt?.duplicate == true) {
                        log.debug("Duplicate audit event {}", receipt.eventId)
                    }
                }
                .doOnError { error ->
                    log.error("Failed to persist audit event from Kafka", error)
                }
                .subscribe()
        } catch (error: Exception) {
            log.error("Invalid audit payload from Kafka", error)
        }
    }
}