package io.auditplatform.server

import io.auditplatform.auditor.AuditEvent
import io.auditplatform.auditor.AuditReceipt
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/audit-events")
class AuditController(
    private val ingestionService: AuditIngestionService,
    private val repository: AuditEventRepository
) {

    @PostMapping
    fun ingest(@RequestBody event: AuditEvent): Mono<ResponseEntity<AuditReceipt>> =
        ingestionService.ingest(event).map { ResponseEntity.ok(it) }

    @GetMapping("/{eventId}")
    fun getById(@PathVariable eventId: String): Mono<ResponseEntity<AuditDocument>> =
        repository.findById(eventId)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())

    @GetMapping("/entities/{entityType}/{entityId}")
    fun history(
        @RequestHeader("X-Tenant-Id") tenantId: String,
        @PathVariable entityType: String,
        @PathVariable entityId: String,
        @RequestParam(defaultValue = "100") limit: Long
    ): Flux<AuditDocument> = repository
        .findAllByTenantIdAndEntityTypeAndEntityIdOrderByOccurredAtDesc(tenantId, entityType, entityId)
        .take(limit.coerceIn(1, 1000))

    @GetMapping("/actors/{actorId}")
    fun byActor(
        @RequestHeader("X-Tenant-Id") tenantId: String,
        @PathVariable actorId: String,
        @RequestParam(defaultValue = "100") limit: Long
    ): Flux<AuditDocument> = repository
        .findAllByTenantIdAndActorIdOrderByOccurredAtDesc(tenantId, actorId)
        .take(limit.coerceIn(1, 1000))
}
