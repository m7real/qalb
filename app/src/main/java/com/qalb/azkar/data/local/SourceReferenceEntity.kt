package com.qalb.azkar.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Source reference attached to a Dhikr (docs/DATA_MODEL.md §3).
 * Rows are never fabricated at runtime: only curated content carries them.
 */
@Entity(
    tableName = "source_references",
    foreignKeys = [
        ForeignKey(
            entity = DhikrEntity::class,
            parentColumns = ["id"],
            childColumns = ["dhikr_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("dhikr_id")],
)
data class SourceReferenceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "dhikr_id") val dhikrId: String,
    /** QURAN | HADITH_COLLECTION | SCHOLARLY | OTHER */
    @ColumnInfo(name = "source_type") val sourceType: String,
    @ColumnInfo(name = "source_name") val sourceName: String,
    @ColumnInfo(name = "reference_text") val referenceText: String?,
    @ColumnInfo(name = "url") val url: String?,
    @ColumnInfo(name = "grading_text") val gradingText: String?,
    /** ReviewStatus enum name: VERIFIED | NEEDS_REVIEW */
    @ColumnInfo(name = "review_status") val reviewStatus: String,
    @ColumnInfo(name = "review_note") val reviewNote: String?,
    @ColumnInfo(name = "reviewed_at") val reviewedAt: Long?,
)
