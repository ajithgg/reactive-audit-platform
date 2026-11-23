package io.auditplatform.server

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@ConditionalOnProperty(prefix = "audit.ingestion", name = ["api-key"])
class AuditIngestionApiKeyWebFilter(
    private val properties: AuditIngestionProperties
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (exchange.request.method != HttpMethod.POST) {
            return chain.filter(exchange)
        }
        val path = exchange.request.path.value()
        if (path != "/api/v1/audit-events") {
            return chain.filter(exchange)
        }

        val provided = exchange.request.headers.getFirst(HEADER_NAME)
        val expected = properties.apiKey
        if (expected.isNullOrBlank() || provided != expected) {
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            return exchange.response.setComplete()
        }
        return chain.filter(exchange)
    }

    companion object {
        const val HEADER_NAME = "X-Audit-Ingest-Key"
    }
}