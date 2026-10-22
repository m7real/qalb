package com.qalb.azkar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DhikrEntity::class,
        SourceReferenceEntity::class,
        CollectionEntity::class,
        CollectionItemEntity::class,
        SeedStateEntity::class,
    ],
    version = AzkarDatabase.SCHEMA_VERSION,
    exportSchema = true,
)
abstract class AzkarDatabase : RoomDatabase() {

    abstract fun dhikrDao(): DhikrDao
    abstract fun sourceReferenceDao(): SourceReferenceDao
    abstract fun collectionDao(): CollectionDao
    abstract fun seedStateDao(): SeedStateDao

    companion object {
        const val SCHEMA_VERSION = 1
        private const val DB_NAME = "qalb.db"

        fun create(context: Context): AzkarDatabase =
            Room.databaseBuilder(context, AzkarDatabase::class.java, DB_NAME).build()

        /**
         * Versioned migrations live here as the schema evolves. Schema dumps for
         * every past version are committed under app/schemas/ — a migration must
         * be written against those exact dumps and covered by a test before the
         * version number is ever incremented.
         */
        val MIGRATIONS: Array<androidx.room.migration.Migration> = arrayOf()
    }
}
