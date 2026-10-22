package com.qalb.azkar.domain.editor

import com.qalb.azkar.domain.model.ContentClass
import com.qalb.azkar.domain.model.ContentOrigin
import com.qalb.azkar.domain.model.OccasionType

/**
 * Draft handed in by the personal-entry editor screen.
 * Everything is user-owned; there is deliberately NO review-status field here —
 * users cannot bestow authenticity on their own words.
 */
data class DhikrDraft(
    val arabicText: String,
    val transliteration: String?,
    val bengaliMeaning: String?,
    val notes: String?,
    val plannedRepeatCount: Int?,
    val occasionType: OccasionType?,
)

sealed class EditorialCheck {
    data object Ok : EditorialCheck()
    data class Rejected(val reasons: List<String>) : EditorialCheck()
}

/**
 * Editorial rules applied to every create/update of a personal dhikr
 * (docs/AZKAR_CONTENT_POLICY.md). Keeping them here keeps them out of
 * Composables and makes them unit-testable without Android.
 */
object DhikrEditorRules {

    /** Hard cap so a typo cannot create, say, 999,999 repetitions silently. */
    const val MAX_REPEAT_COUNT = 10_000
    const val MAX_TEXT_LENGTH = 8_000

    fun checkCreate(draft: DhikrDraft): EditorialCheck = check(draft, allowBlankArabic = false)

    /**
     * Curated wording is immutable in-app: the only fields a user may change on
     * a curated row are the personal note and the favourite flag, applied
     * elsewhere. This predicate is what the repository consults before doing
     * any other update on a curated row.
     */
    fun canEditWording(contentClass: ContentClass): Boolean =
        contentClass == ContentClass.USER_CREATED

    private fun check(draft: DhikrDraft, allowBlankArabic: Boolean): EditorialCheck {
        val reasons = mutableListOf<String>()
        if (draft.arabicText.isBlank()) {
            if (!allowBlankArabic) reasons += "Arabic text must not be empty"
        }
        if (draft.arabicText.length > MAX_TEXT_LENGTH) {
            reasons += "Arabic text is too long"
        }
        draft.transliteration?.let {
            if (it.length > MAX_TEXT_LENGTH) reasons += "Transliteration is too long"
        }
        draft.bengaliMeaning?.let {
            if (it.length > MAX_TEXT_LENGTH) reasons += "Bengali meaning is too long"
        }
        draft.notes?.let {
            if (it.length > MAX_TEXT_LENGTH) reasons += "Notes are too long"
        }
        val repeat = draft.plannedRepeatCount
        if (repeat != null && repeat < 1) {
            reasons += "Repetition must be at least 1 (or empty)"
        }
        if (repeat != null && repeat > MAX_REPEAT_COUNT) {
            reasons += "Repetition is unreasonably large"
        }
        return if (reasons.isEmpty()) EditorialCheck.Ok else EditorialCheck.Rejected(reasons)
    }

    /** Rows are always stored as USER_CREATED/USER_CONTENT when user-created. */
    fun originForUserEntry(): ContentOrigin = ContentOrigin.USER_CREATED
}
