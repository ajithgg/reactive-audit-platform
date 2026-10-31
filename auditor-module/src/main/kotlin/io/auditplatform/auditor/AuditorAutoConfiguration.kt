package io.auditplatform.auditor

import tools.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@AutoConfiguration
@EnableConfigurationProperties(AuditorProperties::class)
@ConditionalOnProperty(prefix = "auditor", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class AuditorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun auditContextWebFilter(): AuditContextWebFilter = AuditContextWebFilter()

    @Bean
    @ConditionalOnMissingBean
    fun auditRedactor(properties: AuditorProperties): AuditRedactor = AuditRedactor(properties.redactedFields)

    @Bean
    @ConditionalOnMissingBean
    fun jsonSnapshotMapper(objectMapper: ObjectMapper, redactor: AuditRedactor): JsonSnapshotMapper =
        JsonSnapshotMapper(objectMapper, redactor)

    @Bean
    @ConditionalOnMissingBean
    fun jsonDiffEngine(objectMapper: ObjectMapper): JsonDiffEngine = JsonDiffEngine(objectMapper)

    @Bean
    @ConditionalOnMissingBean(WebClient.Builder::class)
    fun webClientBuilder(): WebClient.Builder = WebClient.builder()

    @Bean
    @ConditionalOnMissingBean(AuditPublisher::class)
    fun auditPublisher(properties: AuditorProperties, builder: WebClient.Builder): AuditPublisher =
        HttpAuditPublisher(builder.baseUrl(properties.endpoint).build(), properties)

    @Bean
    @ConditionalOnMissingBean
    fun auditor(
        properties: AuditorProperties,
        publisher: AuditPublisher,
        snapshotMapper: JsonSnapshotMapper,
        diffEngine: JsonDiffEngine
    ): Auditor = Auditor(properties, publisher, snapshotMapper, diffEngine)
}
