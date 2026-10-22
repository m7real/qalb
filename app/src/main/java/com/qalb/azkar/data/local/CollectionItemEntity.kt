package com.qalb.azkar.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Ordered membership of a Dhikr in a Collection.
 *
 * Decision recorded for the open design point in docs/DATA_MODEL.md:
 * the FK targets the Dhikr row itself. Collections are ordered presentations,
 * not occurrences; copying wording or occasion here would create duplicates
 * that drift apart. Deleting either side deletes the membership row.
 */
@Entity(
    tableName = "collection_items",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collection_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = DhikrEntity::class,
            parentColumns = ["id"],
            childColumns = ["dhikr_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("collection_id"),
        Index("dhikr_id"),
        Index(value = ["collection_id", "dhikr_id"], unique = true),
    ],
)
data class CollectionItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "collection_id") val collectionId: String,
    @ColumnInfo(name = "dhikr_id") val dhikrId: String,
    /** Collection-local presentation order; stable sort by (sort_order, id). */
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "enabled") val enabled: Boolean,
)
