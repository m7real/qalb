package com.qalb.azkar.domain.seed

import com.qalb.azkar.domain.model.ContentOrigin
import com.qalb.azkar.domain.model.OccasionType
import com.qalb.azkar.domain.model.ReviewStatus

/**
 * One Dhikr as it appears in a bundled seed file, BEFORE it becomes a row.
 * Parsing lives in the data layer; this file holds only vocabulary-free data
 * and pure merge/validate rules (unit tested without Android).
 */
data class SeedSource(
    val sourceId: String,
    val sourceType: String,
    val sourceName: String,
    val referenceText: String?,
    val url: String?,
    val gradingText: String?,
    val reviewStatus: ReviewStatus,
    val reviewNote: String?,
    val reviewedAt: Long?,
)

data class SeedDhikr(
    val id: String,
    val arabicText: String,
    val transliteration: String?,
    val bengaliMeaning: String?,
    val notes: String?,
    val origin: ContentOrigin,
    val reviewStatus: ReviewStatus,
    val plannedRepeatCount: Int?,
    val occasionType: OccasionType?,
    val sources: List<SeedSource>,
)

data class SeedFile(
    val seedSchemaVersion: Int,
    val generatedAt: String?,
    val dhikrs: List<SeedDhikr>,
)

/** A non-fatal but blocking problem found in a seed file. */
data class SeedProblem(val seedId: String?, val reason: String)

/**
 * Seed application policy (docs/DATA_MODEL.md §5):
 *
 *  1. INSERT-ONLY. A seed file NEVER updates or deletes existing rows.
 *     Re-applying the same or newer file must not change any stored wording.
 *     This is what guarantees a seed update can never silently overwrite a
 *     user's edited content.
 *  2. Blocks are caught BEFORE any row is written (validate()).
 *  3. Whether the file is applied at all is decided by seed_state, which is
 *     repository concern, not part of these pure rules.
 */
object SeedMerge {

    /** Rows whose id is not yet present. Insert-only: contents never merge. */
    fun filterNew(seed: SeedFile, existingIds: Set<String>): List<SeedDhikr> =
        seed.dhikrs.filter { it.id !in existingIds }

    /**
     * Structural validation. Returns every problem found; empty list = file is
     * safe to apply (after filterNew). Note: fabricated-looking content cannot
     * be detected mechanically — the seeding policy instead FORCES unreviewed
     * wording to stay NEEDS_REVIEW (see ContentClass.from), so nothing invalid
     * can ever be displayed as verified.
     */
    fun validate(seed: SeedFile): List<SeedProblem> {
        val problems = mutableListOf<SeedProblem>()
        if (seed.seedSchemaVersion < 1) {
            problems += SeedProblem(null, "seedSchemaVersion must be >= 1")
        }
        val seen = mutableSetOf<String>()
        for (dhikr in seed.dhikrs) {
            if (!seen.add(dhikr.id)) {
                problems += SeedProblem(dhikr.id, "duplicate id inside seed file")
                continue
            }
            if (dhikr.arabicText.isBlank()) {
                problems += SeedProblem(dhikr.id, "arabicText must not be blank")
            }
            if (dhikr.reviewStatus == ReviewStatus.VERIFIED && dhikr.sources.isEmpty()) {
                problems += SeedProblem(
                    dhikr.id,
                    "VERIFIED seed row must carry at least one source reference",
                )
            }
            dhikr.sources.forEach { source ->
                if (source.sourceName.isBlank()) {
                    problems += SeedProblem(dhikr.id, "source sourceName must not be blank")
                }
                if (source.reviewStatus == ReviewStatus.VERIFIED && source.reviewedAt == null) {
                    problems += SeedProblem(dhikr.id, "VERIFIED source must carry reviewedAt")
                }
            }
        }
        return problems
    }
}
