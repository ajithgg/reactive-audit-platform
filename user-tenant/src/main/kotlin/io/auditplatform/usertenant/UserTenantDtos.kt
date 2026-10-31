package io.auditplatform.usertenant

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class CreateUserRequest(
    @field:NotBlank val externalId: String,
    @field:Email @field:NotBlank val email: String,
    @field:NotBlank val displayName: String,
    val status: String = "ACTIVE",
    val attributes: Map<String, Any?> = emptyMap()
)

data class UpdateUserRequest(
    @field:Email val email: String? = null,
    val displayName: String? = null,
    val status: String? = null,
    val attributes: Map<String, Any?>? = null
)

data class UserResponse(
    val id: UUID,
    val tenantId: String,
    val externalId: String,
    val email: String,
    val displayName: String,
    val status: String,
    val attributes: Map<String, Any?>,
    val version: Long,
    val createdAt: Instant,
    val updatedAt: Instant
)
