package com.qalb.azkar.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Dhikr record (docs/DATA_MODEL.md §1).
 *
 * Enum fields are stored as the enum NAME string, never as an ordinal —
 * inserting a new enum value in a later release must never reinterpret
 * existing rows.
 */
@Entity(tableName = "dhikr")
data class DhikrEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "arabic_text") val arabicText: String,
    @ColumnInfo(name = "transliteration") val transliteration: String?,
    @ColumnInfo(name = "bengali_meaning") val bengaliMeaning: String?,
    @ColumnInfo(name = "notes") val notes: String?,
    /** ContentOrigin enum name: CURATED | USER_CREATED */
    @ColumnInfo(name = "origin") val origin: String,
    /** ReviewStatus enum name: VERIFIED | NEEDS_REVIEW | USER_CONTENT */
    @ColumnInfo(name = "review_status") val reviewStatus: String,
    @ColumnInfo(name = "is_favourite") val isFavourite: Boolean,
    /** Personal/editor default repetition count. Scheduling reads it later. */
    @ColumnInfo(name = "planned_repeat_count") val plannedRepeatCount: Int?,
    /** OccasionType enum name, nullable. Not a prayer-time binding. */
    @ColumnInfo(name = "occasion_type") val occasionType: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
