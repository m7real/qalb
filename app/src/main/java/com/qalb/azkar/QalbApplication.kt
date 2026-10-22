package com.qalb.azkar

import android.app.Application
import android.util.Log
import com.qalb.azkar.data.local.AzkarDatabase
import com.qalb.azkar.data.repository.LibraryRepository
import com.qalb.azkar.data.repository.SeedRepository
import com.qalb.azkar.data.seed.SeedAssetSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Owns the singletons. Deliberately dependency-free (no DI framework) while
 * the project is single-module: plain lazy properties are the simplest thing
 * that satisfies docs/APP_ARCHITECTURE.md's repository boundary.
 */
class QalbApplication : Application() {

    val database: AzkarDatabase by lazy { AzkarDatabase.create(this) }

    val libraryRepository: LibraryRepository by lazy {
        LibraryRepository(
            dhikrDao = database.dhikrDao(),
            sourceDao = database.sourceReferenceDao(),
            collectionDao = database.collectionDao(),
        )
    }

    val seedRepository: SeedRepository by lazy {
        SeedRepository(database, SeedAssetSource.fromAssets(this))
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            try {
                seedRepository.applyBundledSeedIfNeeded()
            } catch (e: Exception) {
                // Seed problems must not brick the app (the user's own content
                // is still usable); the failure is logged loudly so it shows in
                // the first debugging session.
                Log.e("QalbSeed", "applyBundledSeedIfNeeded failed", e)
            }
        }
    }
}
