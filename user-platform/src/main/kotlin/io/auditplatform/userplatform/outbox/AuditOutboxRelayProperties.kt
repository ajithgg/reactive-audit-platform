package io.auditplatform.userplatform.outbox

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auditor.outbox.relay")
data class AuditOutboxRelayProperties(
    var enabled: Boolean = true,
    var batchSize: Int = 50,
    var fixedDelayMs: Long = 2000,
    var maxAttempts: Int = 10
)