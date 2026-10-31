package io.auditplatform.usertenant

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class UserPlatformClient(
    builder: WebClient.Builder,
    @Value("\${user-platform.base-url}") baseUrl: String
) {
    private val client = builder.baseUrl(baseUrl).build()

    fun create(context: TenantRequestContext, request: CreateUserRequest): Mono<UserResponse> =
        client.post()
            .uri("/platform/v1/users")
            .headers { context.applyTo(it) }
            .bodyValue(request)
            .retrieve()
            .bodyToMono(UserResponse::class.java)

    fun get(context: TenantRequestContext, id: UUID): Mono<UserResponse> =
        client.get()
            .uri("/platform/v1/users/{id}", id)
            .headers { context.applyTo(it) }
            .retrieve()
            .bodyToMono(UserResponse::class.java)

    fun list(context: TenantRequestContext): Mono<List<UserResponse>> =
        client.get()
            .uri("/platform/v1/users")
            .headers { context.applyTo(it) }
            .retrieve()
            .bodyToFlux(UserResponse::class.java)
            .collectList()

    fun update(context: TenantRequestContext, id: UUID, request: UpdateUserRequest): Mono<UserResponse> =
        client.patch()
            .uri("/platform/v1/users/{id}", id)
            .headers { context.applyTo(it) }
            .bodyValue(request)
            .retrieve()
            .bodyToMono(UserResponse::class.java)

    fun delete(context: TenantRequestContext, id: UUID): Mono<Void> =
        client.delete()
            .uri("/platform/v1/users/{id}", id)
            .headers { context.applyTo(it) }
            .retrieve()
            .bodyToMono(Void::class.java)
}
