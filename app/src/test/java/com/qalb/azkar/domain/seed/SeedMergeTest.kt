package com.qalb.azkar.domain.seed

import com.qalb.azkar.domain.model.ContentOrigin
import com.qalb.azkar.domain.model.OccasionType
import com.qalb.azkar.domain.model.ReviewStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private fun sampleDhikr(
    id: String = "sample-1",
    origin: ContentOrigin = ContentOrigin.CURATED,
    reviewStatus: ReviewStatus = ReviewStatus.NEEDS_REVIEW,
    sources: List<SeedSource> = emptyList(),
    arabicText: String = "النص",
) = SeedDhikr(
    id = id,
    arabicText = arabicText,
    transliteration = null,
    bengaliMeaning = null,
    notes = null,
    origin = origin,
    reviewStatus = reviewStatus,
    plannedRepeatCount = null,
    occasionType = OccasionType.ANYTIME,
    sources = sources,
)

class SeedMergeTest {

    @Test
    fun `valid seed file passes validation`() {
        val file = SeedFile(
            seedSchemaVersion = 1,
            generatedAt = "2026-10-22",
            dhikrs = listOf(sampleDhikr(id = "a"), sampleDhikr(id = "b")),
        )
        assertTrue(SeedMerge.validate(file).isEmpty())
    }

    @Test
    fun `duplicate ids inside the file are rejected`() {
        val file = SeedFile(1, null, listOf(sampleDhikr(id = "a"), sampleDhikr(id = "a")))
        val problems = SeedMerge.validate(file)
        assertEquals(1, problems.size)
        assertEquals("duplicate id inside seed file", problems.first().reason)
    }

    @Test
    fun `blank arabic text is rejected`() {
        val file = SeedFile(1, null, listOf(sampleDhikr(arabicText = "  ")))
        val problems = SeedMerge.validate(file)
        assertEquals(1, problems.size)
        assertTrue(problems.first().reason.contains("arabicText"))
    }

    @Test
    fun `verified curated row without any source is rejected`() {
        val file = SeedFile(
            1,
            null,
            listOf(sampleDhikr(id = "v", reviewStatus = ReviewStatus.VERIFIED)),
        )
        val problems = SeedMerge.validate(file)
        assertTrue(problems.any { it.reason.contains("VERIFIED seed row") })
    }

    @Test
    fun `verified source without review date is rejected`() {
        val source = SeedSource(
            sourceId = "s1",
            sourceType = "HADITH_COLLECTION",
            sourceName = "Sahih Muslim",
            referenceText = null,
            url = null,
            gradingText = null,
            reviewStatus = ReviewStatus.VERIFIED,
            reviewNote = null,
            reviewedAt = null,
        )
        val file = SeedFile(
            1,
            null,
            listOf(sampleDhikr(id = "v", reviewStatus = ReviewStatus.VERIFIED, sources = listOf(source))),
        )
        val problems = SeedMerge.validate(file)
        assertTrue(problems.any { it.reason.contains("reviewedAt") })
    }

    @Test
    fun `schema version below one is rejected`() {
        val file = SeedFile(0, null, emptyList())
        val problems = SeedMerge.validate(file)
        assertTrue(problems.any { it.seedId == null })
    }

    @Test
    fun `filterNew is insert-only and never modifies content`() {
        val file = SeedFile(1, null, listOf(sampleDhikr(id = "a"), sampleDhikr(id = "b")))
        val fresh = SeedMerge.filterNew(file, existingIds = setOf("a"))
        assertEquals(listOf("b"), fresh.map { it.id })
    }

    @Test
    fun `filterNew excludes every existing id regardless of content differences`() {
        // Simulates a user-edited row with a seed id: re-seeding must not touch it.
        val file = SeedFile(1, null, listOf(sampleDhikr(id = "a")))
        val fresh = SeedMerge.filterNew(file, existingIds = setOf("a"))
        assertTrue(fresh.isEmpty())
    }
}
