package com.qalb.azkar.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SeedStateDao {

    @Query("SELECT * FROM seed_state WHERE id = :id")
    suspend fun getById(id: Int = SeedStateEntity.SINGLETON_ID): SeedStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: SeedStateEntity)
}
