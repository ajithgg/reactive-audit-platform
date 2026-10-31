package io.auditplatform.userplatform

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UserPlatformApplication

fun main(args: Array<String>) {
    runApplication<UserPlatformApplication>(*args)
}
