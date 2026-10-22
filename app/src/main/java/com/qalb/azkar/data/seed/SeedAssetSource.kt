package com.qalb.azkar.data.seed

import android.content.Context

/** Abstraction over asset reading so tests can inject text directly. */
fun interface SeedAssetSource {
    fun openText(path: String): String

    companion object {
        /** Reads from the APK's assets directory. */
        fun fromAssets(context: Context): SeedAssetSource =
            SeedAssetSource { path ->
                context.assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
            }
    }
}
