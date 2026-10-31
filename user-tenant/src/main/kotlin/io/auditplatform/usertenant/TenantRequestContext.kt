package io.auditplatform.usertenant

import org.springframework.http.HttpHeaders
import java.util.UUID

data class TenantRequestContext(
    val tenantId: String,
    val actorId: String?,
    val actorType: String,
    val correlationId: String,
    val requestId: String?
) {
    fun applyTo(headers: HttpHeaders) {
        headers.set("X-Tenant-Id", tenantId)
        actorId?.let { headers.set("X-Actor-Id", it) }
        headers.set("X-Actor-Type", actorType)
        headers.set("X-Correlation-Id", correlationId)
        requestId?.let { headers.set("X-Request-Id", it) }
    }

    companion object {
        fun from(
            tenantId: String,
            actorId: String?,
            actorType: String?,
            correlationId: String?,
            requestId: String?
        ) = TenantRequestContext(
            tenantId = tenantId,
            actorId = actorId,
            actorType = actorType ?: "USER",
            correlationId = correlationId ?: UUID.randomUUID().toString(),
            requestId = requestId
        )
    }
}
