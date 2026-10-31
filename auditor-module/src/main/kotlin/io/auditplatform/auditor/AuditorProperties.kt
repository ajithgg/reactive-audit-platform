package io.auditplatform.auditor

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auditor")
data class AuditorProperties(
    var enabled: Boolean = true,
    var endpoint: String = "http://localhost:8083",
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
