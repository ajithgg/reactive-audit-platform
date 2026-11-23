package io.auditplatform.auditor.outbox

import tools.jackson.databind.ObjectMapper
import io.auditplatform.auditor.AuditPublisher
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.r2dbc.core.DatabaseClient
import io.r2dbc.spi.ConnectionFactory

@AutoConfiguration
@ConditionalOnClass(AuditPublisher::class)
@ConditionalOnProperty(prefix = "auditor", name = ["publisher"], havingValue = "outbox")
class OutboxAuditorAutoConfiguration {

    @Bean
    fun auditOutboxDatabaseClient(connectionFactory: ConnectionFactory): DatabaseClient =
        DatabaseClient.create(connectionFactory)

    @Bean
    fun r2dbcOutboxAuditPublisher(
        auditOutboxDatabaseClient: DatabaseClient,
        objectMapper: ObjectMapper
    ): AuditPublisher = R2dbcOutboxAuditPublisher(auditOutboxDatabaseClient, objectMapper)
}