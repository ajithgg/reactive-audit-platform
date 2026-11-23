package io.auditplatform.auditor.outbox

enum class AuditOutboxStatus {
    PENDING,
    SENT,
    FAILED
}