package com.qalb.azkar.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row (id = SINGLETON_ID) record of which bundled seed version has
 * already been applied. Combined with insert-only seeding this guarantees a
 * seed update can never silently overwrite the user's edited content.
 */
@Entity(tableName = "seed_state")
data class SeedStateEntity(
    @PrimaryKey val id: Int = SeedStateEntity.SINGLETON_ID,
    @ColumnInfo(name = "applied_seed_version") val appliedSeedVersion: Int,
    @ColumnInfo(name = "applied_at") val appliedAt: Long,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
