package io.auditplatform.auditor

import reactor.core.publisher.Mono

fun interface AuditPublisher {
    fun publish(event: AuditEvent): Mono<AuditReceipt>
}
