package com.qalb.azkar.data.repository

import com.qalb.azkar.data.local.CollectionDao
import com.qalb.azkar.data.local.CollectionItemEntity
import com.qalb.azkar.data.local.CollectionEntity
import com.qalb.azkar.data.local.DhikrDao
import com.qalb.azkar.data.local.DhikrEntity
import com.qalb.azkar.data.local.toDomain
import com.qalb.azkar.domain.editor.DhikrDraft
import com.qalb.azkar.domain.editor.DhikrEditorRules
import com.qalb.azkar.domain.editor.EditorialCheck
import com.qalb.azkar.domain.model.Collection
import com.qalb.azkar.domain.model.CollectionDhikr
import com.qalb.azkar.domain.model.ContentClass
import com.qalb.azkar.domain.model.ContentOrigin
import com.qalb.azkar.domain.model.Dhikr
import com.qalb.azkar.domain.model.ReviewStatus
import com.qalb.azkar.domain.model.SourceReference
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Library list filter. Pure presentation-level filtering of verified content class. */
enum class LibraryFilter { ALL, VERIFIED_CURATED, NEEDS_REVIEW, USER_CREATED, FAVOURITES }

/** Which way a collection row is moved. */
enum class MoveDirection { UP, DOWN }

sealed class SaveResult {
    data object Saved : SaveResult()
    data class Rejected(val reasons: List<String>) : SaveResult()
}

data class OperationFailure(val reason: String)

/**
 * All library CRUD. This is the only class that mutates dhikr rows, so the
 * editorial policy ("user content can never be marked verified", "curated
 * wording is immutable in-app") is enforced here, in the domain of the rules —
 * not in the UI.
 */
class LibraryRepository(
    private val dhikrDao: DhikrDao,
    private val sourceDao: com.qalb.azkar.data.local.SourceReferenceDao,
    private val collectionDao: CollectionDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {

    // ---------- Reading ----------

    fun observeLibrary(query: String, filter: LibraryFilter): Flow<List<Dhikr>> =
        dhikrDao.search(query.trim()).map { rows ->
            rows.asSequence()
                .map { it.toDomain() }
                .filter { matchesFilter(it, filter) }
                .toList()
        }

    fun observeDhikr(id: String): Flow<Dhikr?> =
        dhikrDao.observeById(id).map { it?.toDomain() }

    fun observeSources(dhikrId: String): Flow<List<SourceReference>> =
        sourceDao.observeByDhikr(dhikrId).map { rows -> rows.map { it.toDomain() } }

    fun observeCollections(): Flow<List<Collection>> =
        collectionDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    fun observeCollectionContent(collectionId: String): Flow<List<CollectionDhikr>> =
        combine(
            collectionDao.observeItems(collectionId),
            dhikrDao.observeAll(),
        ) { items, dhikrs ->
            val byId = dhikrs.associateBy { it.id }
            items.mapNotNull { item ->
                byId[item.dhikrId]?.let { entity ->
                    CollectionDhikr(item = item.toDomain(), dhikr = entity.toDomain())
                }
            }
        }

    private fun matchesFilter(dhikr: Dhikr, filter: LibraryFilter): Boolean = when (filter) {
        LibraryFilter.ALL -> true
        LibraryFilter.FAVOURITES -> dhikr.isFavourite
        LibraryFilter.VERIFIED_CURATED ->
            dhikr.contentClass == ContentClass.VERIFIED_CURATED
        LibraryFilter.NEEDS_REVIEW ->
            dhikr.contentClass == ContentClass.NEEDS_REVIEW
        LibraryFilter.USER_CREATED ->
            dhikr.contentClass == ContentClass.USER_CREATED
    }

    // ---------- Personal entry create/update/delete ----------

    suspend fun createPersonalEntry(draft: DhikrDraft): SaveResult {
        when (val check = DhikrEditorRules.checkCreate(draft)) {
            is EditorialCheck.Rejected -> return SaveResult.Rejected(check.reasons)
            EditorialCheck.Ok -> Unit
        }
        val now = clock()
        val id = UUID.randomUUID().toString()
        dhikrDao.insert(
            DhikrEntity(
                id = id,
                arabicText = draft.arabicText.trim(),
                transliteration = draft.transliteration?.trim()?.ifEmpty { null },
                bengaliMeaning = draft.bengaliMeaning?.trim()?.ifEmpty { null },
                notes = draft.notes?.trim()?.ifEmpty { null },
                // USER_CREATED origin forces USER_CONTENT classification downstream;
                // a personal entry can never be presented as verified.
                origin = ContentOrigin.USER_CREATED.name,
                reviewStatus = ReviewStatus.USER_CONTENT.name,
                isFavourite = false,
                plannedRepeatCount = draft.plannedRepeatCount,
                occasionType = draft.occasionType?.name,
                createdAt = now,
                updatedAt = now,
            ),
        )
        return SaveResult.Saved
    }

    suspend fun updatePersonalEntry(id: String, draft: DhikrDraft): SaveResult {
        val entity = dhikrDao.getById(id) ?: return SaveResult.Rejected(listOf("Entry no longer exists"))
        val contentClass = entity.toDomain().contentClass
        if (!DhikrEditorRules.canEditWording(contentClass)) {
            return SaveResult.Rejected(
                listOf("Curated wording is immutable; only your personal entries can be edited"),
            )
        }
        when (val check = DhikrEditorRules.checkCreate(draft)) {
            is EditorialCheck.Rejected -> return SaveResult.Rejected(check.reasons)
            EditorialCheck.Ok -> Unit
        }
        dhikrDao.updateWordingFields(
            id = id,
            arabicText = draft.arabicText.trim(),
            transliteration = draft.transliteration?.trim()?.ifEmpty { null },
            bengaliMeaning = draft.bengaliMeaning?.trim()?.ifEmpty { null },
            notes = draft.notes?.trim()?.ifEmpty { null },
            plannedRepeatCount = draft.plannedRepeatCount,
            occasionType = draft.occasionType?.name,
            updatedAt = clock(),
        )
        return SaveResult.Saved
    }

    suspend fun deletePersonalEntry(id: String): OperationFailure? {
        val entity = dhikrDao.getById(id) ?: return OperationFailure("Entry no longer exists")
        val contentClass = entity.toDomain().contentClass
        if (contentClass != ContentClass.USER_CREATED) {
            return OperationFailure("Only personal entries can be deleted")
        }
        dhikrDao.deleteById(id)
        return null
    }

    // ---------- Personal overlays (allowed even on curated rows) ----------

    suspend fun setFavourite(id: String, favourite: Boolean) {
        dhikrDao.setFavourite(id, favourite)
    }

    suspend fun updatePersonalNote(id: String, note: String?): OperationFailure? {
        if (note != null && note.length > DhikrEditorRules.MAX_TEXT_LENGTH) {
            return OperationFailure("Note is too long")
        }
        val entity = dhikrDao.getById(id) ?: return OperationFailure("Entry no longer exists")
        dhikrDao.updateNotes(id, note?.trim()?.ifEmpty { null }, maxOf(entity.updatedAt, clock()))
        return null
    }

    // ---------- Collections ----------

    suspend fun createCollection(name: String, description: String?): OperationFailure? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return OperationFailure("Collection name must not be empty")
        if (trimmed.length > MAX_COLLECTION_NAME_LENGTH) {
            return OperationFailure("Collection name is too long")
        }
        collectionDao.insert(
            CollectionEntity(
                id = UUID.randomUUID().toString(),
                name = trimmed,
                description = description?.trim()?.ifEmpty { null },
                createdAt = clock(),
            ),
        )
        return null
    }

    suspend fun deleteCollection(id: String) {
        collectionDao.deleteById(id)
    }

    /** Append at the end; a duplicate membership is refused, not silently merged. */
    suspend fun addToCollection(collectionId: String, dhikrId: String): OperationFailure? {
        val existing = collectionDao.getItemsNow(collectionId)
        if (existing.any { it.dhikrId == dhikrId }) {
            return OperationFailure("Already in this collection")
        }
        val nextOrder = (collectionDao.maxSortOrder(collectionId) ?: -1) + 1
        collectionDao.insertItem(
            CollectionItemEntity(
                id = UUID.randomUUID().toString(),
                collectionId = collectionId,
                dhikrId = dhikrId,
                sortOrder = nextOrder,
                enabled = true,
            ),
        )
        return null
    }

    suspend fun removeFromCollection(itemId: String) {
        collectionDao.deleteItemById(itemId)
    }

    suspend fun setCollectionItemEnabled(itemId: String, enabled: Boolean) {
        collectionDao.setItemEnabled(itemId, enabled)
    }

    /**
     * Re-ordering is a swap of the adjacent row, followed by renumbering so
     * sort orders stay contiguous 0..n-1. Tested in LibraryRepositoryTest.
     */
    suspend fun moveCollectionItem(collectionId: String, itemId: String, direction: MoveDirection) {
        val items = collectionDao.getItemsNow(collectionId)
        val index = items.indexOfFirst { it.id == itemId }
        if (index < 0) return
        val targetIndex = when (direction) {
            MoveDirection.UP -> index - 1
            MoveDirection.DOWN -> index + 1
        }
        if (targetIndex < 0 || targetIndex >= items.size) return

        val reordered = items.toMutableList().apply {
            val tmp = this[index]
            this[index] = this[targetIndex]
            this[targetIndex] = tmp
        }
        renumber(reordered.map { it.id }, collectionId)
    }

    private suspend fun renumber(itemIdsInOrder: List<String>, collectionId: String) {
        // Defensive: drop ids that disappeared concurrently, then renumber what came back.
        val stillThere = collectionDao.getItemsNow(collectionId).map { it.id }.toSet()
        var order = 0
        for (id in itemIdsInOrder) {
            if (id in stillThere) {
                collectionDao.updateItemSortOrder(id, order)
                order++
            }
        }
    }

    companion object {
        const val MAX_COLLECTION_NAME_LENGTH = 120
    }
}
