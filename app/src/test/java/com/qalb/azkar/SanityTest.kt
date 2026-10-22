package com.qalb.azkar

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Minimal sanity test so the unit-test Gradle task is non-empty on day one.
 * Real scheduling / completion / counting / import-export tests arrive with
 * their milestones (see docs/TEST_PLAN.md).
 */
class SanityTest {
    @Test
    fun arithmeticSanity() {
        assertEquals(4, 2 + 2)
    }
}
