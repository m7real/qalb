package com.qalb.azkar.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.qalb.azkar.data.local.AzkarDatabase
import com.qalb.azkar.data.local.DhikrEntity
import com.qalb.azkar.data.local.SourceReferenceEntity
import com.qalb.azkar.data.local.toDomain
import com.qalb.azkar.data.repository.CollectionDhikr
import com.qalb.azkar.data.repository.LibraryFilter
import com.qalb.azkar.data.repository.LibraryRepository
import com.qalb.azkar.data.repository.MoveDirection
import com.qalb.azkar.data.repository.SaveResult
import com.qalb.azkar.domain.editor.DhikrDraft
import com.qalb.azkar.domain.model.ContentClass
import com.qalb.azkar.domain.model.ContentOrigin
import com.qalb.azkar.domain.model.OccasionType
import com.qalb.azkar.domain.model.ReviewStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Persistence tests on a real (in-memory) Room database via Robolectric:
 * personal CRUD, editorial guards, search, collections with ordering, and
 * FK cascade behaviour.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LibraryPersistenceTest {

    private lateinit var context: Context
    private lateinit var database: AzkarDatabase
    private lateinit var repository: LibraryRepository

    private val personalDraft = DhikrDraft(
        arabicText = "  لَا إِلَٰهَ إِلَّا اللَّهُ  ",
        transliteration = "La ilaha illallah",
        bengaliMeaning = null,
        notes = "personal note",
        plannedRepeatCount = 10,
        occasionType = OccasionType.ANYTIME,
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AzkarDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = LibraryRepository(
            dhikrDao = database.dhikrDao(),
            sourceDao = database.sourceReferenceDao(),
            collectionDao = database.collectionDao(),
            clock = { 1_000L },
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun insertPersonal(vararg drafts: DhikrDraft): List<String> {
        drafts.forEach { draft ->
            assertEquals(SaveResult.Saved, repository.createPersonalEntry(draft))
        }
        return repository.observeLibrary("", LibraryFilter.ALL).first().map { it.id }
    }

    // ---------- Personal CRUD ----------

    @Test
    fun `create persists a personal entry with user-content classification`() = runBlocking {
        repository.createPersonalEntry(personalDraft)
        val entries = repository.observeLibrary("", LibraryFilter.ALL).first()
        assertEquals(1, entries.size)
        val saved = entries.first()
        assertEquals(ContentClass.USER_CREATED, saved.contentClass)
        // Trimmed on the way in.
        assertEquals("لَا إِلَٰهَ إِلَّا اللَّهُ", saved.arabicText)
        assertEquals(10, saved.plannedRepeatCount)
        assertEquals(OccasionType.ANYTIME, saved.occasionType)
        assertFalse(saved.isFavourite)
    }

    @Test
    fun `blank arabic text is rejected and nothing is stored`() = runBlocking {
        val result = repository.createPersonalEntry(personalDraft.copy(arabicText = " "))
        assertTrue(result is SaveResult.Rejected)
        assertEquals(0, repository.observeLibrary("", LibraryFilter.ALL).first().size)
    }

    @Test
    fun `update changes writable fields on a personal entry`() = runBlocking {
        val id = insertPersonal(personalDraft).single()
        val result = repository.updatePersonalEntry(
            id,
            personalDraft.copy(arabicText = "نص جديد", plannedRepeatCount = 5, notes = null),
        )
        assertEquals(SaveResult.Saved, result)
        val updated = repository.observeDhikr(id).first()!!
        assertEquals("نص جديد", updated.arabicText)
        assertEquals(5, updated.plannedRepeatCount)
        assertEquals(null, updated.notes)
        // Still, and always, personal content.
        assertEquals(ContentClass.USER_CREATED, updated.contentClass)
    }

    @Test
    fun `personal entry can be deleted`() = runBlocking {
        val id = insertPersonal(personalDraft).single()
        assertEquals(null, repository.deletePersonalEntry(id))
        assertTrue(repository.observeLibrary("", LibraryFilter.ALL).first().isEmpty())
    }

    // ---------- Curated rows are protected ----------

    @Test
    fun `curated wording cannot be edited through the repository`() = runBlocking {
        val curated = curatedEntity("cur-1")
        database.dhikrDao().insert(curated)
        val result = repository.updatePersonalEntry(
            "cur-1",
            DhikrDraft("خُطًى مُزَيَّف", null, null, null, null, null),
        )
        assertTrue(result is SaveResult.Rejected)
        // Wording unchanged:
        assertEquals(
            curated.arabicText,
            repository.observeDhikr("cur-1").first()!!.arabicText,
        )
    }

    @Test
    fun `curated row cannot be deleted through the repository`() = runBlocking {
        database.dhikrDao().insert(curatedEntity("cur-2"))
        val failure = repository.deletePersonalEntry("cur-2")
        assertTrue(failure != null)
        assertEquals(1, repository.observeLibrary("", LibraryFilter.ALL).first().size)
    }

    @Test
    fun `personal overlays on curated rows are allowed`() = runBlocking {
        database.dhikrDao().insert(curatedEntity("cur-3"))
        repository.updatePersonalNote("cur-3", "আমার নোট")
        repository.setFavourite("cur-3", true)
        val row = repository.observeDhikr("cur-3").first()!!
        assertEquals("আমার নোট", row.notes)
        assertTrue(row.isFavourite)
        assertEquals(ContentClass.NEEDS_REVIEW, row.contentClass)
    }

    @Test
    fun `corrupt enum names fall back to needs-review, never verified`() = runBlocking {
        database.dhikrDao().insert(
            curatedEntity("bad-1").copy(origin = "IMPORTED", reviewStatus = "SUNNAH"),
        )
        val row = repository.observeLibrary("", LibraryFilter.ALL).first().single()
        assertEquals(ContentClass.NEEDS_REVIEW, row.contentClass)
    }

    // ---------- Search and filters ----------

    @Test
    fun `search matches transliteration and meaning`() = runBlocking {
        insertPersonal(
            personalDraft.copy(arabicText = "ب", transliteration = null, bengaliMeaning = "নমুনা অর্থ",
                notes = null, occasionType = null, plannedRepeatCount = null),
            personalDraft.copy(arabicText = "গ", transliteration = "token-unique",
                bengaliMeaning = null, notes = null, occasionType = null, plannedRepeatCount = null),
        )
        assertEquals(1, repository.observeLibrary("নমুনা", LibraryFilter.ALL).first().size)
        assertEquals(1, repository.observeLibrary("token-unique", LibraryFilter.ALL).first().size)
        // Empty query matches everything:
        assertEquals(2, repository.observeLibrary("", LibraryFilter.ALL).first().size)
    }

    @Test
    fun `filter chips narrow the list by content class and favourites`() = runBlocking {
        val ids = insertPersonal(personalDraft)
        database.dhikrDao().insert(curatedEntity("cur-4", arabicText = "ক"))
        val personalId = ids.single()
        repository.setFavourite(personalId, true)

        val all = repository.observeLibrary("", LibraryFilter.ALL).first()
        assertEquals(2, all.size)
        assertEquals(1, repository.observeLibrary("", LibraryFilter.USER_CREATED).first().size)
        assertEquals(1, repository.observeLibrary("", LibraryFilter.NEEDS_REVIEW).first().size)
        assertEquals(0, repository.observeLibrary("", LibraryFilter.VERIFIED_CURATED).first().size)
        assertEquals(1, repository.observeLibrary("", LibraryFilter.FAVOURITES).first().size)
    }

    // ---------- Collections ----------

    @Test
    fun `collection membership, duplicate refusal and removal work`() = runBlocking {
        val ids = insertPersonal(
            personalDraft.copy(arabicText = "১", transliteration = null, notes = null,
                bengaliMeaning = null, occasionType = null, plannedRepeatCount = null),
            personalDraft.copy(arabicText = "২", transliteration = null, notes = null,
                bengaliMeaning = null, occasionType = null, plannedRepeatCount = null),
        )
        assertEquals(null, repository.createCollection("রোজার সংগ্রহ", null))
        val collections = repository.observeCollections().first()
        val collectionId = collections.single().id

        ids.forEach { id -> assertEquals(null, repository.addToCollection(collectionId, id)) }
        // Duplicate membership is refused.
        assertTrue(repository.addToCollection(collectionId, ids[0]) != null)
        assertEquals(2, repository.observeCollectionContent(collectionId).first().size)

        repository.removeFromCollection(
            repository.observeCollectionContent(collectionId).first().first().item.id,
        )
        assertEquals(1, repository.observeCollectionContent(collectionId).first().size)
    }

    @Test
    fun `moving an item up and down reorders correctly and stays contiguous`() = runBlocking {
        val ids = insertPersonal(
            personalDraft.copy(arabicText = "ক", transliteration = null, notes = null,
                bengaliMeaning = null, occasionType = null, plannedRepeatCount = null),
            personalDraft.copy(arabicText = "খ", transliteration = null, notes = null,
                bengaliMeaning = null, occasionType = null, plannedRepeatCount = null),
            personalDraft.copy(arabicText = "গ", transliteration = null, notes = null,
                bengaliMeaning = null, occasionType = null, plannedRepeatCount = null),
        )
        repository.createCollection("ক", null)
        val collectionId = repository.observeCollections().first().single().id
        ids.forEach { id -> repository.addToCollection(collectionId, id) }

        val itemIdsByText = repository.observeCollectionContent(collectionId).first()
            .associate { it.dhikr.arabicText to it.item.id }
        val items: suspend () -> List<CollectionDhikr> = { repository.observeCollectionContent(collectionId).first() }

        repository.moveCollectionItem(collectionId, itemIdsByText.getValue("গ"), MoveDirection.UP)
        // Expected: specificity by arabic text: ক, গ, খ
        assertEquals(listOf("ক", "গ", "খ"), items().map { it.dhikr.arabicText })
        assertEquals(listOf(0, 1, 2), items().map { it.item.sortOrder })

        // Moving the first item up is a no-op.
        repository.moveCollectionItem(collectionId, items().first().item.id, MoveDirection.UP)
        assertEquals(listOf("ক", "গ", "খ"), items().map { it.dhikr.arabicText })

        repository.moveCollectionItem(collectionId, itemIdsByText.getValue("ক"), MoveDirection.DOWN)
        assertEquals(listOf("গ", "ক", "খ"), items().map { it.dhikr.arabicText })
        assertEquals(listOf(0, 1, 2), items().map { it.item.sortOrder })
    }

    @Test
    fun `deleting a collection keeps its dhikr entries`() = runBlocking {
        val ids = insertPersonal(personalDraft.copy(arabicText = "ঘ", transliteration = null,
            bengaliMeaning = null, notes = null, occasionType = null, plannedRepeatCount = null))
        repository.createCollection("temp", null)
        val collectionId = repository.observeCollections().first().single().id
        repository.addToCollection(collectionId, ids.single())

        repository.deleteCollection(collectionId)
        assertTrue(repository.observeCollections().first().isEmpty())
        assertEquals(0, repository.observeCollectionContent(collectionId).first().size)
        // The dhikr itself survives:
        assertEquals(1, repository.observeLibrary("", LibraryFilter.ALL).first().size)
    }

    // ---------- Cascade and source refs ----------

    @Test
    fun `deleting a dhikr cascades source references and collection memberships`() = runBlocking {
        val id = insertPersonal(personalDraft.copy(arabicText = "ঙ", transliteration = null,
            bengaliMeaning = null, notes = null, occasionType = null, plannedRepeatCount = null)).single()
        database.sourceReferenceDao().insertAll(
            listOf(
                SourceReferenceEntity(
                    id = "ref-1", dhikrId = id, sourceType = "QURAN",
                    sourceName = "Quran", referenceText = "corpus-ref-1",
                    url = null, gradingText = null,
                    reviewStatus = ReviewStatus.NEEDS_REVIEW.name,
                    reviewNote = null, reviewedAt = null,
                ),
            ),
        )
        repository.createCollection("म", null)
        val collectionId = repository.observeCollections().first().single().id
        repository.addToCollection(collectionId, id)
        assertEquals(1, database.sourceReferenceDao().count())
        assertEquals(1, database.collectionDao().getItemsNow(collectionId).size)

        repository.deletePersonalEntry(id)
        assertEquals(0, database.sourceReferenceDao().count())
        assertEquals(0, database.collectionDao().getItemsNow(collectionId).size)
        assertEquals(0, repository.observeLibrary("", LibraryFilter.ALL).first().size)
    }

    @Test
    fun `classification survives a database reopen`() = runBlocking {
        val context2 = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "reopen-test.db"
        context2.deleteDatabase(dbName)
        val first = Room.databaseBuilder(context2, AzkarDatabase::class.java, dbName)
            .allowMainThreadQueries().build()
        val repo1 = LibraryRepository(
            first.dhikrDao(), first.sourceReferenceDao(), first.collectionDao(), clock = { 1L },
        )
        assertEquals(SaveResult.Saved, repo1.createPersonalEntry(personalDraft))
        first.close()

        val second = Room.databaseBuilder(context2, AzkarDatabase::class.java, dbName)
            .allowMainThreadQueries().build()
        val repo2 = LibraryRepository(
            second.dhikrDao(), second.sourceReferenceDao(), second.collectionDao(), clock = { 1L },
        )
        val entries = repo2.observeLibrary("", LibraryFilter.ALL).first()
        assertEquals(1, entries.size)
        assertEquals(ContentClass.USER_CREATED, entries.single().contentClass)
        second.close()
        context2.deleteDatabase(dbName)
    }

    private fun curatedEntity(id: String, arabicText: String = "curated-arabic") = DhikrEntity(
        id = id,
        arabicText = arabicText,
        transliteration = "curated-translit",
        bengaliMeaning = null,
        notes = null,
        origin = ContentOrigin.CURATED.name,
        reviewStatus = ReviewStatus.NEEDS_REVIEW.name,
        isFavourite = false,
        plannedRepeatCount = null,
        occasionType = OccasionType.ANYTIME.name,
        createdAt = 1L,
        updatedAt = 1L,
    )
}
