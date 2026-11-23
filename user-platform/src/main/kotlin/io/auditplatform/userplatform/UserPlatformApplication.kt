package io.auditplatform.userplatform

import io.auditplatform.userplatform.outbox.AuditOutboxRelayProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AuditOutboxRelayProperties::class)
@EnableR2dbcRepositories(
    basePackages = [
        "io.auditplatform.userplatform",
        "io.auditplatform.auditor.outbox"
    ]
)
class UserPlatformApplication

fun main(args: Array<String>) {
    runApplication<UserPlatformApplication>(*args)
}
