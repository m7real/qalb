package com.qalb.azkar.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DhikrDao {

    @Query("SELECT * FROM dhikr ORDER BY updated_at DESC, id ASC")
    fun observeAll(): Flow<List<DhikrEntity>>

    /**
     * SQL-backed search (docs/PRODUCT_SPEC.md "search"). LIKE with literal
     * concatenation; Arabic/Bengali have no case-folding needs, so unadorned
     * LIKE is the honest behaviour. Filtered further in the repository.
     */
    @Query(
        """
        SELECT * FROM dhikr
        WHERE :query = ''
           OR arabic_text LIKE '%' || :query || '%'
           OR COALESCE(transliteration, '') LIKE '%' || :query || '%'
           OR COALESCE(bengali_meaning, '') LIKE '%' || :query || '%'
           OR COALESCE(notes, '') LIKE '%' || :query || '%'
        ORDER BY updated_at DESC, id ASC
        """,
    )
    fun search(query: String): Flow<List<DhikrEntity>>

    @Query("SELECT * FROM dhikr WHERE id = :id")
    fun observeById(id: String): Flow<DhikrEntity?>

    @Query("SELECT * FROM dhikr WHERE id = :id")
    suspend fun getById(id: String): DhikrEntity?

    @Query("SELECT * FROM dhikr WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<DhikrEntity>

    @Query("SELECT id FROM dhikr")
    suspend fun getAllIds(): List<String>

    /** INSERT for brand-new rows only; a conflicting id aborts loudly. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: DhikrEntity)

    /** Full-row update for USER_CREATED rows (guarded in the repository). */
    @Update
    suspend fun update(entity: DhikrEntity)

    @Query("DELETE FROM dhikr WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("UPDATE dhikr SET is_favourite = :favourite WHERE id = :id")
    suspend fun setFavourite(id: String, favourite: Boolean)

    /** Personal note is a user-owned overlay — even on curated rows. */
    @Query("UPDATE dhikr SET notes = :notes, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateNotes(id: String, notes: String?, updatedAt: Long)

    @Query(
        """
        UPDATE dhikr
        SET arabic_text = :arabicText,
            transliteration = :transliteration,
            bengali_meaning = :bengaliMeaning,
            notes = :notes,
            planned_repeat_count = :plannedRepeatCount,
            occasion_type = :occasionType,
            updated_at = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateWordingFields(
        id: String,
        arabicText: String,
        transliteration: String?,
        bengaliMeaning: String?,
        notes: String?,
        plannedRepeatCount: Int?,
        occasionType: String?,
        updatedAt: Long,
    )
}

@Dao
interface SourceReferenceDao {

    @Query("SELECT * FROM source_references WHERE dhikr_id = :dhikrId ORDER BY id ASC")
    fun observeByDhikr(dhikrId: String): Flow<List<SourceReferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(sources: List<SourceReferenceEntity>)

    @Query("DELETE FROM source_references WHERE dhikr_id = :dhikrId")
    suspend fun deleteByDhikr(dhikrId: String)

    @Query("SELECT COUNT(*) FROM source_references")
    suspend fun count(): Int
}

@Dao
interface CollectionDao {

    @Query("SELECT * FROM collections ORDER BY created_at ASC, id ASC")
    fun observeAll(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getById(id: String): CollectionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: CollectionEntity)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("SELECT * FROM collection_items WHERE collection_id = :collectionId ORDER BY sort_order ASC, id ASC")
    fun observeItems(collectionId: String): Flow<List<CollectionItemEntity>>

    @Query("SELECT * FROM collection_items WHERE collection_id = :collectionId ORDER BY sort_order ASC, id ASC")
    suspend fun getItemsNow(collectionId: String): List<CollectionItemEntity>

    /** INSERT for new membership rows only; duplicate (collection, dhikr) aborts. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItem(item: CollectionItemEntity)

    @Query("DELETE FROM collection_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: String): Int

    @Query("UPDATE collection_items SET sort_order = :sortOrder WHERE id = :itemId")
    suspend fun updateItemSortOrder(itemId: String, sortOrder: Int)

    @Query("UPDATE collection_items SET enabled = :enabled WHERE id = :itemId")
    suspend fun setItemEnabled(itemId: String, enabled: Boolean)

    @Query("SELECT MAX(sort_order) FROM collection_items WHERE collection_id = :collectionId")
    suspend fun maxSortOrder(collectionId: String): Int?
}
