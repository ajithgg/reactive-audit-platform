package io.auditplatform.server

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "audit.kafka")
data class AuditKafkaProperties(
    var topic: String = "audit-events",
    var consumer: Consumer = Consumer()
) {
    data class Consumer(
        var enabled: Boolean = false
    )
}