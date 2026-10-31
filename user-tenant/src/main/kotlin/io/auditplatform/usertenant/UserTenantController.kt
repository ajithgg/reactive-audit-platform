package io.auditplatform.usertenant

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
class UserTenantController(
    private val userPlatformClient: UserPlatformClient
) {

    @PostMapping
    fun create(
        @RequestHeader("X-Tenant-Id") tenantId: String,
        @RequestHeader("X-Actor-Id", required = false) actorId: String?,
        @RequestHeader("X-Actor-Type", required = false) actorType: String?,
        @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
        @RequestHeader("X-Request-Id", required = false) requestId: String?,
        @Valid @RequestBody request: CreateUserRequest
    ): Mono<ResponseEntity<UserResponse>> = userPlatformClient
        .create(TenantRequestContext.from(tenantId, actorId, actorType, correlationId, requestId), request)
        .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping("/{id}")
    fun get(
        @RequestHeader("X-Tenant-Id") tenantId: String,
        @RequestHeader("X-Actor-Id", required = false) actorId: String?,
        @RequestHeader("X-Actor-Type", required = false) actorType: String?,
        @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
        @RequestHeader("X-Request-Id", required = false) requestId: String?,
        @PathVariable id: UUID
    ): Mono<UserResponse> = userPlatformClient.get(
        TenantRequestContext.from(tenantId, actorId, actorType, correlationId, requestId), id
    )

    @GetMapping
    fun list(
        @RequestHeader("X-Tenant-Id") tenantId: String,
        @RequestHeader("X-Actor-Id", required = false) actorId: String?,
        @RequestHeader("X-Actor-Type", required = false) actorType: String?,
        @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
        @RequestHeader("X-Request-Id", required = false) requestId: String?
    ): Mono<List<UserResponse>> = userPlatformClient.list(
        TenantRequestContext.from(tenantId, actorId, actorType, correlationId, requestId)
    )

    @PatchMapping("/{id}")
    fun update(
        @RequestHeader("X-Tenant-Id") tenantId: String,
        @RequestHeader("X-Actor-Id", required = false) actorId: String?,
        @RequestHeader("X-Actor-Type", required = false) actorType: String?,
        @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
        @RequestHeader("X-Request-Id", required = false) requestId: String?,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateUserRequest
    ): Mono<UserResponse> = userPlatformClient.update(
        TenantRequestContext.from(tenantId, actorId, actorType, correlationId, requestId), id, request
    )

    @DeleteMapping("/{id}")
    fun delete(
        @RequestHeader("X-Tenant-Id") tenantId: String,
        @RequestHeader("X-Actor-Id", required = false) actorId: String?,
        @RequestHeader("X-Actor-Type", required = false) actorType: String?,
        @RequestHeader("X-Correlation-Id", required = false) correlationId: String?,
        @RequestHeader("X-Request-Id", required = false) requestId: String?,
        @PathVariable id: UUID
    ): Mono<ResponseEntity<Void>> = userPlatformClient.delete(
        TenantRequestContext.from(tenantId, actorId, actorType, correlationId, requestId), id
    ).thenReturn(ResponseEntity.noContent().build())
}
