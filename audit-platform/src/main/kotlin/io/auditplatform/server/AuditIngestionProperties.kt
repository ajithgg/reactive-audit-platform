package io.auditplatform.server

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "audit.ingestion")
data class AuditIngestionProperties(
    /** When set, HTTP POST /api/v1/audit-events requires header X-Audit-Ingest-Key. */
    var apiKey: String? = null
)