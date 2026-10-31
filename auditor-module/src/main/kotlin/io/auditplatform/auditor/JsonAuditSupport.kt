package io.auditplatform.auditor

import tools.jackson.core.type.TypeReference
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.node.ObjectNode

class AuditRedactor(
    private val redactedFields: Set<String>
) {
    fun redact(node: JsonNode?): JsonNode? {
        if (node == null || node.isNull) return node
        val copy = node.deepCopy()
        redactRecursively(copy)
        return copy
    }

    private fun redactRecursively(node: JsonNode) {
        when {
            node.isObject -> {
                val objectNode = node as ObjectNode
                val fields = objectNode.properties().toList()
                fields.forEach { (name, child) ->
                    if (redactedFields.any { it.equals(name, ignoreCase = true) }) {
                        objectNode.put(name, "***REDACTED***")
                    } else {
                        redactRecursively(child)
                    }
                }
            }
            node.isArray -> node.forEach(::redactRecursively)
        }
    }
}

class JsonDiffEngine(
    private val objectMapper: ObjectMapper
) {
    fun diff(before: JsonNode?, after: JsonNode?): List<AuditChange> {
        val changes = mutableListOf<AuditChange>()
        compare("", before, after, changes)
        return changes
    }

    private fun compare(path: String, before: JsonNode?, after: JsonNode?, changes: MutableList<AuditChange>) {
        val beforeMissing = before == null || before.isMissingNode
        val afterMissing = after == null || after.isMissingNode

        if (beforeMissing && afterMissing) return

        if (beforeMissing) {
            changes += AuditChange(
                path = normalizedPath(path),
                operation = AuditChangeOperation.ADD,
                newValue = after?.let(::toValue)
            )
            return
        }

        if (afterMissing) {
            changes += AuditChange(
                path = normalizedPath(path),
                operation = AuditChangeOperation.REMOVE,
                oldValue = before?.let(::toValue)
            )
            return
        }

        val beforeNode = requireNotNull(before)
        val afterNode = requireNotNull(after)

        if (beforeNode == afterNode) return

        if (beforeNode.isObject && afterNode.isObject) {
            val beforeObject = beforeNode as ObjectNode
            val afterObject = afterNode as ObjectNode
            val names = (
                beforeObject.properties().map { it.key }.toSet() +
                    afterObject.properties().map { it.key }.toSet()
                ).sorted()
            names.forEach { field ->
                compare("$path/${escape(field)}", beforeObject.path(field), afterObject.path(field), changes)
            }
            return
        }

        // Arrays are intentionally treated atomically in v1. This avoids unstable index-based diffs.
        changes += AuditChange(
            path = normalizedPath(path),
            operation = AuditChangeOperation.REPLACE,
            oldValue = toValue(beforeNode),
            newValue = toValue(afterNode)
        )
    }

    private fun toValue(node: JsonNode): Any? = objectMapper.convertValue(node, Any::class.java)

    private fun normalizedPath(path: String): String = if (path.isBlank()) "/" else path

    private fun escape(value: String): String = value.replace("~", "~0").replace("/", "~1")
}

class JsonSnapshotMapper(
    private val objectMapper: ObjectMapper,
    private val redactor: AuditRedactor
) {
    fun toRedactedNode(value: Any?): JsonNode? = value?.let { redactor.redact(objectMapper.valueToTree(it)) }

    fun toMap(node: JsonNode?): Map<String, Any?>? {
        if (node == null || node.isNull) return null
        if (node.isObject) {
            return objectMapper.convertValue(node, object : TypeReference<Map<String, Any?>>() {})
        }
        return mapOf("_value" to objectMapper.convertValue(node, Any::class.java))
    }
}
