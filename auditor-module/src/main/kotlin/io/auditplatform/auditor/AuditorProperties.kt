package io.auditplatform.auditor

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auditor")
data class AuditorProperties(
    var enabled: Boolean = true,
    /** `http` = direct POST to audit-platform; `outbox` = same TX write to R2DBC outbox + Kafka relay. */
    var publisher: String = "http",
    /** When false, `recordRead` is a no-op (CREATE / UPDATE / DELETE still audit). */
    var auditReads: Boolean = false,
    var endpoint: String = "http://localhost:8083",
    /** Optional shared secret for HTTP ingestion (header `X-Audit-Ingest-Key`). */
    var ingestApiKey: String? = null,
    var serviceName: String = "unknown-service",
    var environment: String = "local",
    var instanceId: String? = null,
    var failOnError: Boolean = false,
    var retryAttempts: Long = 2,
    var redactedFields: Set<String> = setOf(
        "password",
        "accessToken",
        "refreshToken",
        "authorization",
        "secret"
    )
)
