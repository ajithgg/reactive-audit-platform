package io.auditplatform.userplatform

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("user_record")
data class UserRecord(
    @Id
    val id: UUID,
    val tenantId: String,
    val externalId: String,
    val email: String,
    val displayName: String,
    val status: String,
    val attributesJson: String,
    @Version
    val version: Long? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)
