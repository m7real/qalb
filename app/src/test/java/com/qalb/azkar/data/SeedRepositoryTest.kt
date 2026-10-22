package com.qalb.azkar.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.qalb.azkar.data.local.AzkarDatabase
import com.qalb.azkar.data.local.DhikrEntity
import com.qalb.azkar.data.local.SeedStateEntity
import com.qalb.azkar.data.local.toDomain
import com.qalb.azkar.data.repository.LibraryFilter
import com.qalb.azkar.data.repository.LibraryRepository
import com.qalb.azkar.data.repository.SeedRepository
import com.qalb.azkar.data.seed.SeedAssetSource
import com.qalb.azkar.domain.editor.DhikrDraft
import com.qalb.azkar.domain.model.ContentClass
import com.qalb.azkar.domain.model.ContentOrigin
import com.qalb.azkar.domain.model.ReviewStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Seed application safety (docs/DATA_MODEL.md §5):
 * insert-only, once per version, refuse bad files before writing anything.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SeedRepositoryTest {

    private lateinit var context: Context
    private lateinit var database: AzkarDatabase

    private val realAssetLoader: SeedAssetSource by lazy {
        SeedAssetSource.fromAssets(context)
    }

    private fun seedRepository(loader: SeedAssetSource = realAssetLoader): SeedRepository =
        SeedRepository(database, loader, clock = { 7_000L })

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AzkarDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `bundled asset applies once and is idempotent`() = runBlocking {
        seedRepository().applyBundledSeedIfNeeded()
        seedRepository().applyBundledSeedIfNeeded()

        val all = database.dhikrDao().getAllIds()
        assertEquals(3, all.size)
        val state = database.seedStateDao().getById()
        assertEquals(SeedRepository.BUNDLED_SEED_VERSION, state?.appliedSeedVersion)
        assertEquals(7000L, state?.appliedAt)
    }

    @Test
    fun `bundled sample entries are unreviewed curated content`() = runBlocking {
        seedRepository().applyBundledSeedIfNeeded()
        val repo = libraryRepository()
        val entries = repo.observeLibrary("", LibraryFilter.ALL).first()
        assertTrue(entries.isNotEmpty())
        entries.forEach { dhikr ->
            assertEquals(ContentClass.NEEDS_REVIEW, dhikr.contentClass)
            assertFalse(dhikr.isFavourite)
        }
    }

    @Test
    fun `existing rows with seed ids are never overwritten`() = runBlocking {
        // Simulate a row the user (or a future different seed) already owns.
        database.dhikrDao().insert(
            DhikrEntity(
                id = "sample-placeholder-1",
                arabicText = "আমার নিজের লেখা",
                transliteration = null,
                bengaliMeaning = null,
                notes = "user-owned",
                origin = ContentOrigin.USER_CREATED.name,
                reviewStatus = ReviewStatus.USER_CONTENT.name,
                isFavourite = true,
                plannedRepeatCount = 4,
                occasionType = null,
                createdAt = 1L,
                updatedAt = 2L,
            ),
        )

        seedRepository().applyBundledSeedIfNeeded()

        val ids = database.dhikrDao().getAllIds()
        // Insert-only: the pre-existing row stays exactly as it was; only the
        // two other seed placeholders were added.
        assertEquals(3, ids.size)
        val untouched = database.dhikrDao().getById("sample-placeholder-1")!!
        assertEquals("আমার নিজের লেখা", untouched.arabicText)
        assertEquals(1L, untouched.createdAt)
        assertTrue(untouched.isFavourite)
        val classifications = ids.map { database.dhikrDao().getById(it)!!.toDomain().contentClass }
        // The untouched row keeps its classification too.
        assert(classifications.contains(ContentClass.USER_CREATED))
    }

    @Test
    fun `invalid seed file is refused loudly and writes nothing`() = runBlocking {
        val brokenSeed = """
            {"seedSchemaVersion":1,"dhikrs":[
              {"id":"dup","arabicText":"نص","reviewStatus":"NEEDS_REVIEW","sources":[]},
              {"id":"dup","arabicText":"نص","reviewStatus":"NEEDS_REVIEW","sources":[]}
            ]}
        """.trimIndent()
        try {
            seedRepository(fakeLoader(brokenSeed)).applyBundledSeedIfNeeded()
            fail("expected IllegalStateException for duplicate seed ids")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message!!.contains("invalid"))
        }
        assertEquals(0, database.dhikrDao().getAllIds().size)
        assertNull(database.seedStateDao().getById())
        // And no source rows can appear without dhikr rows.
        assertEquals(0, database.sourceReferenceDao().count())
    }

    @Test
    fun `newer schema version than supported is refused`() = runBlocking {
        val futureSeed = """
            {"seedSchemaVersion":99,"dhikrs":[
              {"id":"x","arabicText":"نص","reviewStatus":"NEEDS_REVIEW","sources":[]}
            ]}
        """.trimIndent()
        try {
            seedRepository(fakeLoader(futureSeed)).applyBundledSeedIfNeeded()
            fail("expected IllegalStateException for unsupported seed version")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message!!.contains("newer than supported"))
        }
        assertEquals(0, database.dhikrDao().getAllIds().size)
    }

    @Test
    fun `seed with verified content and a source applies and classifies verified`() = runBlocking {
        val verifiedSeed = """
            {"seedSchemaVersion":1,"dhikrs":[
              {"id":"verified-1","arabicText":"نص موثّق","reviewStatus":"VERIFIED",
               "plannedRepeatCount":1,
               "sources":[{"sourceId":"v1-source","sourceType":"HADITH_COLLECTION",
                           "sourceName":"Sahih Muslim","referenceText":"33-150",
                           "url":null,"gradingText":null,
                           "reviewStatus":"VERIFIED","reviewNote":null,"reviewedAt":1700000000000}]}
            ]}
        """.trimIndent()
        seedRepository(fakeLoader(verifiedSeed)).applyBundledSeedIfNeeded()
        val repo = libraryRepository()
        val entry = repo.observeLibrary("", LibraryFilter.ALL).first().single()
        assertEquals(ContentClass.VERIFIED_CURATED, entry.contentClass)
        val sources = repo.observeSources(entry.id).first()
        assertEquals(1, sources.size)
        assertEquals(ReviewStatus.VERIFIED, sources.single().reviewStatus)
    }

    private fun fakeLoader(text: String): SeedAssetSource = SeedAssetSource { text }

    private fun libraryRepository() = LibraryRepository(
        dhikrDao = database.dhikrDao(),
        sourceDao = database.sourceReferenceDao(),
        collectionDao = database.collectionDao(),
        clock = { 1L },
    )
}
