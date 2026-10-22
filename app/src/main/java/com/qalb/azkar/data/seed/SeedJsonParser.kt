package com.qalb.azkar.data.seed

import com.qalb.azkar.domain.model.ContentOrigin
import com.qalb.azkar.domain.model.OccasionType
import com.qalb.azkar.domain.model.ReviewStatus
import com.qalb.azkar.domain.seed.SeedDhikr
import com.qalb.azkar.domain.seed.SeedFile
import com.qalb.azkar.domain.seed.SeedSource
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * org.json-based seed parser (bundled with Android; no extra dependency).
 * Throws [SeedJsonParser.SeedParseException] with the field name on structure
 * problems. Vocabulary validation (duplicate ids, blank text,
 * VERIFIED-without-source) happens afterwards in SeedMerge.validate.
 */
object SeedJsonParser {

    class SeedParseException(message: String, cause: Throwable? = null) :
        Exception(message, cause)

    fun parse(text: String): SeedFile = try {
        parseRoot(JSONObject(text))
    } catch (e: JSONException) {
        throw SeedParseException("Seed file is not valid JSON: ${e.message}", e)
    }

    private fun parseRoot(root: JSONObject): SeedFile {
        val version = parseInt(root, "seedSchemaVersion", required = true)!!
        val generatedAt = optNullableString(root, "generatedAt")
        val array = requireArray(root, "dhikrs")
        val dhikrs = sequenceDhikrs(array)
        return SeedFile(
            seedSchemaVersion = version,
            generatedAt = generatedAt,
            dhikrs = dhikrs,
        )
    }

    private fun sequenceDhikrs(array: JSONArray): List<SeedDhikr> {
        val result = mutableListOf<SeedDhikr>()
        for (i in 0 until array.length()) {
            result += parseDhikr(array.getJSONObject(i))
        }
        return result
    }

    private fun parseDhikr(obj: JSONObject): SeedDhikr {
        val sources = parseSources(obj.optJSONArray("sources") ?: JSONArray())
        return SeedDhikr(
            id = requireString(obj, "id"),
            arabicText = requireString(obj, "arabicText"),
            transliteration = optNullableString(obj, "transliteration"),
            bengaliMeaning = optNullableString(obj, "bengaliMeaning"),
            notes = optNullableString(obj, "notes"),
            origin = parseContentOrigin(obj),
            reviewStatus = parseReviewStatus(obj, "reviewStatus", default = ReviewStatus.NEEDS_REVIEW),
            plannedRepeatCount = parseInt(obj, "plannedRepeatCount", required = false),
            occasionType = optNullableString(obj, "occasionType")?.let { name ->
                OccasionType.fromNameOrNull(name)
                    ?: throw SeedParseException(
                        "occasionType value '$name' (item '${obj.opt("id")}') is not a known OccasionType",
                    )
            },
            sources = sources,
        )
    }

    private fun parseSources(array: JSONArray): List<SeedSource> {
        val result = mutableListOf<SeedSource>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result += SeedSource(
                sourceId = requireString(obj, "sourceId"),
                sourceType = requireString(obj, "sourceType"),
                sourceName = requireString(obj, "sourceName"),
                referenceText = optNullableString(obj, "referenceText"),
                url = optNullableString(obj, "url"),
                gradingText = optNullableString(obj, "gradingText"),
                reviewStatus = parseReviewStatus(obj, "reviewStatus", default = ReviewStatus.NEEDS_REVIEW),
                reviewNote = optNullableString(obj, "reviewNote"),
                reviewedAt = parseLong(obj, "reviewedAt", required = false),
            )
        }
        return result
    }

    private fun parseContentOrigin(obj: JSONObject): ContentOrigin {
        val name = optNullableString(obj, "origin") ?: return ContentOrigin.CURATED
        return ContentOrigin.fromNameOrNull(name)
            ?: throw SeedParseException(
                "Value '$name' at 'origin' (item '${obj.opt("id")}') is not a known ContentOrigin",
            )
    }

    private fun parseReviewStatus(obj: JSONObject, field: String, default: ReviewStatus): ReviewStatus {
        val name = optNullableString(obj, field) ?: return default
        return ReviewStatus.fromNameOrNull(name)
            ?: throw SeedParseException(
                "Value '$name' at '$field' (item '${obj.opt("id")}') is not a known ReviewStatus",
            )
    }

    // --- small org.json helpers that produce useful error messages ---

    private fun requireString(obj: JSONObject, field: String): String =
        optNullableString(obj, field) ?: throw SeedParseException("Missing or blank required field '$field'")

    private fun requireArray(obj: JSONObject, field: String): JSONArray =
        obj.optJSONArray(field) ?: throw SeedParseException("Missing required array field '$field'")

    private fun optNullableString(obj: JSONObject, field: String): String? {
        if (obj.isNull(field)) return null
        val value = obj.optString(field, null)
        return value?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun parseInt(obj: JSONObject, field: String, required: Boolean): Int? {
        if (obj.isNull(field) || !obj.has(field)) {
            if (required) throw SeedParseException("Missing required integer field '$field'")
            return null
        }
        return try {
            obj.getInt(field)
        } catch (e: JSONException) {
            throw SeedParseException("Field '$field' is not an integer", e)
        }
    }

    private fun parseLong(obj: JSONObject, field: String, required: Boolean): Long? {
        if (obj.isNull(field) || !obj.has(field)) {
            if (required) throw SeedParseException("Missing required long field '$field'")
            return null
        }
        return try {
            obj.getLong(field)
        } catch (e: JSONException) {
            throw SeedParseException("Field '$field' is not a long", e)
        }
    }
}
