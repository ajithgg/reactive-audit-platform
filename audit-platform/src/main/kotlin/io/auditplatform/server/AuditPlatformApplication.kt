package io.auditplatform.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka

@SpringBootApplication
@EnableKafka
@EnableConfigurationProperties(AuditIngestionProperties::class, AuditKafkaProperties::class)
class AuditPlatformApplication

fun main(args: Array<String>) {
    runApplication<AuditPlatformApplication>(*args)
}
