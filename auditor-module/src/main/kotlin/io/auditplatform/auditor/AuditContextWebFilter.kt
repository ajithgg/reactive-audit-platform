package io.auditplatform.auditor

import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class AuditContextWebFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val headers = exchange.request.headers
        val actorId = headers.getFirst(HEADER_ACTOR_ID)
        val actorType = headers.getFirst(HEADER_ACTOR_TYPE) ?: "USER"
        val traceId = headers.getFirst(HEADER_TRACE_ID)
            ?: headers.getFirst("traceparent")?.split("-")?.getOrNull(1)

        val context = AuditExecutionContext(
            tenantId = headers.getFirst(HEADER_TENANT_ID),
            actor = actorId?.let { AuditActor(id = it, type = actorType) },
            request = AuditRequestContext(
                method = exchange.request.method.name(),
                path = exchange.request.path.value(),
                correlationId = headers.getFirst(HEADER_CORRELATION_ID),
                traceId = traceId,
                requestId = headers.getFirst(HEADER_REQUEST_ID)
            )
        )

        return chain.filter(exchange)
            .contextWrite { reactorContext -> reactorContext.put(CONTEXT_KEY, context) }
    }

    companion object {
        const val CONTEXT_KEY = "audit.execution.context"
        const val HEADER_TENANT_ID = "X-Tenant-Id"
        const val HEADER_ACTOR_ID = "X-Actor-Id"
        const val HEADER_ACTOR_TYPE = "X-Actor-Type"
        const val HEADER_CORRELATION_ID = "X-Correlation-Id"
        const val HEADER_REQUEST_ID = "X-Request-Id"
        const val HEADER_TRACE_ID = "X-Trace-Id"
    }
}
