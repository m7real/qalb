package com.qalb.azkar.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** A user-defined named grouping (docs/DATA_MODEL.md §4). */
@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
