package com.onlyjesus

import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingPlansTest {
    @Test
    fun removeReadingPlanRemovesOnlyMatchingPlan() {
        val first = samplePlan(id = "first")
        val second = samplePlan(id = "second")

        val updated = removeReadingPlan(listOf(first, second), "first")

        assertEquals(listOf(second), updated)
    }

    @Test
    fun removeReadingPlanReturnsOriginalListWhenPlanIsMissing() {
        val first = samplePlan(id = "first")

        val updated = removeReadingPlan(listOf(first), "missing")

        assertEquals(listOf(first), updated)
    }

    private fun samplePlan(id: String): ReadingPlan {
        return ReadingPlan(
            id = id,
            title = "Sample $id",
            description = "Sample description",
            versionPath = "/tmp/version",
            versionLabel = "KJV",
            createdAt = 1L,
            updatedAt = 1L,
            days = emptyList()
        )
    }
}
