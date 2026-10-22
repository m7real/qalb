package com.qalb.azkar.data.seed

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.qalb.azkar.domain.seed.SeedMerge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric tests for the org.json-based parser plus the real bundled asset.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SeedJsonParserTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `parses the real bundled seed asset and it validates cleanly`() {
        val text = context.assets.open(SeedAssetPath.TEST_PATH).use {
            it.bufferedReader(Charsets.UTF_8).readText()
        }
        val file = SeedJsonParser.parse(text)
        assertEquals(1, file.seedSchemaVersion)
        assertTrue(file.dhikrs.isNotEmpty())
        assertTrue(
            "bundled asset must pass SeedMerge.validate",
            SeedMerge.validate(file).isEmpty(),
        )
    }

    @Test
    fun `unknown enum name fails loudly`() {
        val text = """
            {"seedSchemaVersion":1,"dhikrs":[
              {"id":"a","arabicText":"نص","origin":"IMPORTED","reviewStatus":"NEEDS_REVIEW","sources":[]}
            ]}
        """.trimIndent()
        try {
            SeedJsonParser.parse(text)
            throw AssertionError("expected SeedParseException")
        } catch (e: SeedJsonParser.SeedParseException) {
            assertTrue(e.message!!.contains("origin"))
        }
    }

    @Test
    fun `missing required field fails loudly`() {
        val text = """
            {"seedSchemaVersion":1,"dhikrs":[
              {"id":"a","reviewStatus":"NEEDS_REVIEW","sources":[]}
            ]}
        """.trimIndent()
        try {
            SeedJsonParser.parse(text)
            throw AssertionError("expected SeedParseException")
        } catch (e: SeedJsonParser.SeedParseException) {
            assertTrue(e.message!!.contains("arabicText"))
        }
    }

    @Test
    fun `nulls and absent optionals parse to null`() {
        val text = """
            {"seedSchemaVersion":1,"dhikrs":[
              {"id":"a","arabicText":"نص","transliteration":null,
               "bengaliMeaning":"বাংলা","notes":null,
               "plannedRepeatCount":null,"occasionType":null,"sources":[]}
            ]}
        """.trimIndent()
        val file = SeedJsonParser.parse(text)
        val dhikr = file.dhikrs.single()
        assertEquals("a", dhikr.id)
        assertEquals(null, dhikr.transliteration)
        assertEquals("বাংলা", dhikr.bengaliMeaning)
        assertEquals(null, dhikr.notes)
        assertEquals(null, dhikr.plannedRepeatCount)
        assertEquals(null, dhikr.occasionType)
    }

    @Test
    fun `malformed json fails loudly`() {
        try {
            SeedJsonParser.parse("{not json")
            throw AssertionError("expected SeedParseException")
        } catch (e: SeedJsonParser.SeedParseException) {
            assertTrue(e.message!!.contains("not valid JSON"))
        }
    }

    private object SeedAssetPath {
        // Keep the literal in sync with SeedRepository.SEED_ASSET_PATH.
        const val TEST_PATH = "seed/azkar-library-v1.json"
    }
}
