package io.auditplatform.usertenant

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
class UserTenantApplication {
    // Spring Boot 4 no longer auto-registers WebClient.Builder; provide one for outbound calls.
    @Bean
    fun webClientBuilder(): WebClient.Builder = WebClient.builder()
}

fun main(args: Array<String>) {
    runApplication<UserTenantApplication>(*args)
}
