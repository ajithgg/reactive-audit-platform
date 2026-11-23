package io.auditplatform.auditor.outbox

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("audit_outbox")
data class AuditOutboxEntry(
    @Id
    val id: UUID = UUID.randomUUID(),
    val eventId: String,
    val tenantId: String,
    val payloadJson: String,
    val status: String = AuditOutboxStatus.PENDING.name,
    val createdAt: Instant = Instant.now(),
    val sentAt: Instant? = null,
    val attempts: Int = 0,
    val lastError: String? = null
)