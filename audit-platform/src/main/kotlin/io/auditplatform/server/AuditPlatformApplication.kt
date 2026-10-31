package io.auditplatform.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AuditPlatformApplication

fun main(args: Array<String>) {
    runApplication<AuditPlatformApplication>(*args)
}
