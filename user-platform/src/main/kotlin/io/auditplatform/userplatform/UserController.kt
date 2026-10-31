package io.auditplatform.userplatform

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/platform/v1/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping
    fun create(
        @RequestHeader("X-Tenant-Id") tenantId: String,
        @Valid @RequestBody request: CreateUserRequest
    ): Mono<ResponseEntity<UserResponse>> = userService.create(tenantId, request)
        .map { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping("/{id}")
    fun get(
        @RequestHeader("X-Tenant-Id") tenantId: String,
        @PathVariable id: UUID
    ): Mono<UserResponse> = userService.get(tenantId, id)

    @GetMapping
    fun list(
        @RequestHeader("X-Tenant-Id") tenantId: String
    ): Mono<List<UserResponse>> = userService.list(tenantId)

    @PatchMapping("/{id}")
    fun update(
        @RequestHeader("X-Tenant-Id") tenantId: String,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateUserRequest
    ): Mono<UserResponse> = userService.update(tenantId, id, request)

    @DeleteMapping("/{id}")
    fun delete(
        @RequestHeader("X-Tenant-Id") tenantId: String,
        @PathVariable id: UUID
    ): Mono<ResponseEntity<Void>> = userService.delete(tenantId, id)
        .thenReturn(ResponseEntity.noContent().build())
}

@RestControllerAdvice
class UserExceptionHandler {
    @ExceptionHandler(UserNotFoundException::class)
    fun notFound(ex: UserNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to ex.message.orEmpty()))

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun conflict(ex: UserAlreadyExistsException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to ex.message.orEmpty()))
}
