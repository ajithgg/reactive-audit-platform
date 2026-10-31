package io.auditplatform.userplatform

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

interface UserRecordRepository : ReactiveCrudRepository<UserRecord, UUID> {
    fun findByIdAndTenantId(id: UUID, tenantId: String): Mono<UserRecord>
    fun findAllByTenantIdOrderByCreatedAtDesc(tenantId: String): Flux<UserRecord>
    fun existsByTenantIdAndExternalId(tenantId: String, externalId: String): Mono<Boolean>
}
