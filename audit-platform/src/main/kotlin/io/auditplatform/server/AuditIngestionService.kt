package io.auditplatform.server

import io.auditplatform.auditor.AuditEvent
import io.auditplatform.auditor.AuditReceipt
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AuditIngestionService(
    private val repository: AuditEventRepository
) {
    fun ingest(event: AuditEvent): Mono<AuditReceipt> {
        return repository.findById(event.eventId)
            .map {
                AuditReceipt(
                    eventId = event.eventId,
                    accepted = true,
                    duplicate = true,
                    index = INDEX_NAME
                )
            }
            .switchIfEmpty(
                Mono.defer {
                    val document = AuditDocument.from(event)
                    repository.save(document)
                        .map {
                            AuditReceipt(
                                eventId = event.eventId,
                                accepted = true,
                                duplicate = false,
                                index = INDEX_NAME
                            )
                        }
                }
            )
    }

    companion object {
        const val INDEX_NAME = "audit-events-v1"
    }
}
