package io.auditplatform.auditor

import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

class HttpAuditPublisher(
    private val webClient: WebClient,
    private val properties: AuditorProperties
) : AuditPublisher {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(event: AuditEvent): Mono<AuditReceipt> {
        return webClient.post()
            .uri("/api/v1/audit-events")
            .bodyValue(event)
            .retrieve()
            .bodyToMono(AuditReceipt::class.java)
            .retryWhen(
                Retry.backoff(properties.retryAttempts, Duration.ofMillis(100))
                    .maxBackoff(Duration.ofSeconds(1))
            )
            .onErrorResume { error ->
                if (properties.failOnError) {
                    Mono.error(error)
                } else {
                    log.error("Audit event {} could not be published", event.eventId, error)
                    Mono.just(AuditReceipt(eventId = event.eventId, accepted = false))
                }
            }
    }
}
