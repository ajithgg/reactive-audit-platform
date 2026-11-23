package io.auditplatform.userplatform

import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import io.auditplatform.auditor.Auditor
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Instant
import java.util.UUID

@Service
class UserService(
    private val repository: UserRecordRepository,
    private val objectMapper: ObjectMapper,
    private val auditor: Auditor,
    private val transactionalOperator: TransactionalOperator
) {

    fun create(tenantId: String, request: CreateUserRequest): Mono<UserResponse> =
        repository.existsByTenantIdAndExternalId(tenantId, request.externalId)
            .flatMap { exists ->
                if (exists) return@flatMap Mono.error<UserRecord>(UserAlreadyExistsException(request.externalId))

                val now = Instant.now()
                transactionalOperator.transactional(
                    repository.save(
                        UserRecord(
                            id = UUID.randomUUID(),
                            tenantId = tenantId,
                            externalId = request.externalId,
                            email = request.email,
                            displayName = request.displayName,
                            status = request.status,
                            attributesJson = objectMapper.writeValueAsString(request.attributes),
                            version = null,
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                )
            }
            .flatMap { saved ->
                Mono.defer {
                    auditor.recordCreate(
                        entityType = ENTITY_TYPE,
                        entityId = saved.id.toString(),
                        after = toAuditSnapshot(saved),
                        tenantId = tenantId
                    )
                }
                    .publishOn(Schedulers.boundedElastic())
                    .thenReturn(toResponse(saved))
            }

    fun get(tenantId: String, id: UUID): Mono<UserResponse> =
        findRequired(tenantId, id).map(::toResponse)

    fun list(tenantId: String): Mono<List<UserResponse>> =
        repository.findAllByTenantIdOrderByCreatedAtDesc(tenantId)
            .map(::toResponse)
            .collectList()

    fun update(tenantId: String, id: UUID, request: UpdateUserRequest): Mono<UserResponse> =
        findRequired(tenantId, id)
            .flatMap { before ->
                val updated = before.copy(
                    email = request.email ?: before.email,
                    displayName = request.displayName ?: before.displayName,
                    status = request.status ?: before.status,
                    attributesJson = request.attributes?.let(objectMapper::writeValueAsString) ?: before.attributesJson,
                    updatedAt = Instant.now()
                )

                transactionalOperator.transactional(repository.save(updated))
                    .flatMap { saved ->
                        Mono.defer {
                            auditor.recordUpdate(
                                entityType = ENTITY_TYPE,
                                entityId = id.toString(),
                                before = toAuditSnapshot(before),
                                after = toAuditSnapshot(saved),
                                tenantId = tenantId
                            )
                        }
                            .publishOn(Schedulers.boundedElastic())
                            .thenReturn(toResponse(saved))
                    }
            }

    fun delete(tenantId: String, id: UUID): Mono<Void> =
        findRequired(tenantId, id)
            .flatMap { before ->
                transactionalOperator.transactional(repository.delete(before).then(Mono.just(before)))
                    .flatMap {
                        Mono.defer {
                            auditor.recordDelete(
                                entityType = ENTITY_TYPE,
                                entityId = id.toString(),
                                before = toAuditSnapshot(before),
                                tenantId = tenantId
                            )
                        }.publishOn(Schedulers.boundedElastic())
                    }
            }
            .then()

    private fun findRequired(tenantId: String, id: UUID): Mono<UserRecord> =
        repository.findByIdAndTenantId(id, tenantId)
            .switchIfEmpty(Mono.error(UserNotFoundException(id)))

    private fun toResponse(record: UserRecord) = UserResponse(
        id = record.id,
        tenantId = record.tenantId,
        externalId = record.externalId,
        email = record.email,
        displayName = record.displayName,
        status = record.status,
        attributes = parseAttributes(record.attributesJson),
        version = record.version ?: 0,
        createdAt = record.createdAt,
        updatedAt = record.updatedAt
    )

    private fun toAuditSnapshot(record: UserRecord) = UserAuditSnapshot(
        id = record.id,
        tenantId = record.tenantId,
        externalId = record.externalId,
        email = record.email,
        displayName = record.displayName,
        status = record.status,
        attributes = parseAttributes(record.attributesJson),
        version = record.version ?: 0,
        createdAt = record.createdAt,
        updatedAt = record.updatedAt
    )

    private fun parseAttributes(json: String): Map<String, Any?> =
        objectMapper.readValue(json, object : TypeReference<Map<String, Any?>>() {})

    companion object {
        const val ENTITY_TYPE = "UserRecord"
    }
}

class UserNotFoundException(id: UUID) : RuntimeException("User $id was not found")
class UserAlreadyExistsException(externalId: String) : RuntimeException("User with externalId $externalId already exists")