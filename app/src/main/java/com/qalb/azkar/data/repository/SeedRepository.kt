package com.qalb.azkar.data.repository

import androidx.room.withTransaction
import com.qalb.azkar.data.local.AzkarDatabase
import com.qalb.azkar.data.local.DhikrEntity
import com.qalb.azkar.data.local.SeedStateEntity
import com.qalb.azkar.data.local.SourceReferenceEntity
import com.qalb.azkar.data.seed.SeedAssetSource
import com.qalb.azkar.data.seed.SeedJsonParser
import com.qalb.azkar.domain.seed.SeedDhikr
import com.qalb.azkar.domain.seed.SeedMerge
import com.qalb.azkar.domain.seed.SeedSource

/**
 * Applies the bundled seed file (docs/DATA_MODEL.md §5).
 *
 * Guarantees, all covered by SeedRepositoryTest:
 *  - applied at most once per version (seed_state row);
 *  - INSERT-ONLY: existing rows (seeded or user-edited) are never rewritten;
 *  - structurally invalid files fail LOUDLY before anything is written;
 *  - a file whose schema version is newer than this app understands is
 *    refused, so unknown fields can never silently evaporate.
 */
class SeedRepository(
    private val database: AzkarDatabase,
    private val assets: SeedAssetSource,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    suspend fun applyBundledSeedIfNeeded() {
        val state = database.seedStateDao().getById()
        if (state != null && state.appliedSeedVersion >= BUNDLED_SEED_VERSION) return

        val text = assets.openText(SEED_ASSET_PATH)
        val file = SeedJsonParser.parse(text)

        if (file.seedSchemaVersion > SEED_SCHEMA_VERSION_SUPPORTED) {
            throw IllegalStateException(
                "Seed file version ${file.seedSchemaVersion} is newer than supported " +
                    "version $SEED_SCHEMA_VERSION_SUPPORTED — refusing to apply partially",
            )
        }
        val problems = SeedMerge.validate(file)
        if (problems.isNotEmpty()) {
            throw IllegalStateException(
                "Bundled seed file is invalid, refusing to apply: " +
                    problems.joinToString("; ") { "${it.seedId ?: "<file>"}: ${it.reason}" },
            )
        }

        database.withTransaction {
            val existingIds = database.dhikrDao().getAllIds().toSet()
            val fresh = SeedMerge.filterNew(file, existingIds)
            for (seed in fresh) {
                database.dhikrDao().insert(seed.toEntity())
                if (seed.sources.isNotEmpty()) {
                    database.sourceReferenceDao()
                        .insertAll(seed.sources.map { it.toEntity(seed.id) })
                }
            }
            database.seedStateDao().upsert(
                SeedStateEntity(
                    appliedSeedVersion = maxOf(BUNDLED_SEED_VERSION, file.seedSchemaVersion),
                    appliedAt = clock(),
                ),
            )
        }
    }

    private fun SeedDhikr.toEntity() = DhikrEntity(
        id = id,
        arabicText = arabicText,
        transliteration = transliteration,
        bengaliMeaning = bengaliMeaning,
        notes = notes,
        origin = origin.name,
        reviewStatus = reviewStatus.name,
        isFavourite = false,
        plannedRepeatCount = plannedRepeatCount,
        occasionType = occasionType?.name,
        createdAt = clock(),
        updatedAt = clock(),
    )

    private fun SeedSource.toEntity(dhikrId: String) = SourceReferenceEntity(
        id = sourceId,
        dhikrId = dhikrId,
        sourceType = sourceType,
        sourceName = sourceName,
        referenceText = referenceText,
        url = url,
        gradingText = gradingText,
        reviewStatus = reviewStatus.name,
        reviewNote = reviewNote,
        reviewedAt = reviewedAt,
    )

    companion object {
        /** Version of the file bundled in the APK (bump with new seed content). */
        const val BUNDLED_SEED_VERSION = 1

        /** Highest seed schema this app can interpret without losing fields. */
        const val SEED_SCHEMA_VERSION_SUPPORTED = 1

        const val SEED_ASSET_PATH = "seed/azkar-library-v1.json"
    }
}
