package io.auditplatform.server

import io.auditplatform.auditor.AuditChange
import io.auditplatform.auditor.AuditChangeOperation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AuditDocumentChangeMapsTest {

    @Test
    fun `builds changesBefore and changesAfter from field diffs`() {
        val (before, after) = AuditDocument.toChangeMaps(
            listOf(
                AuditChange("/displayName", AuditChangeOperation.REPLACE, "Old", "New"),
                AuditChange("/status", AuditChangeOperation.REPLACE, "ACTIVE", "INACTIVE"),
                AuditChange("/tags", AuditChangeOperation.ADD, null, listOf("a")),
                AuditChange("/obsolete", AuditChangeOperation.REMOVE, true, null),
                AuditChange("/", AuditChangeOperation.ADD, null, mapOf("ignored" to true))
            )
        )

        assertEquals(mapOf("displayName" to "Old", "status" to "ACTIVE", "obsolete" to true), before)
        assertEquals(
            mapOf("displayName" to "New", "status" to "INACTIVE", "tags" to listOf("a")),
            after
        )
    }

    @Test
    fun `converts nested json pointer paths to dotted keys`() {
        assertEquals("attributes.department", AuditDocument.pathToFieldKey("/attributes/department"))
        assertNull(AuditDocument.pathToFieldKey("/"))
    }
}
