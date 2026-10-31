package io.auditplatform.auditor

import tools.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JsonDiffEngineTest {
    private val mapper = jacksonObjectMapper()
    private val engine = JsonDiffEngine(mapper)

    @Test
    fun `detects changed added and removed fields`() {
        val before = mapper.readTree("""{"name":"A","status":"ACTIVE","obsolete":true}""")
        val after = mapper.readTree("""{"name":"A","status":"INACTIVE","newField":10}""")

        val changes = engine.diff(before, after)

        assertEquals(3, changes.size)
        assertEquals(setOf("/status", "/obsolete", "/newField"), changes.map { it.path }.toSet())
    }
}
