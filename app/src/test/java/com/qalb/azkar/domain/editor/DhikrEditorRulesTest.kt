package com.qalb.azkar.domain.editor

import com.qalb.azkar.domain.model.ContentClass
import com.qalb.azkar.domain.model.OccasionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DhikrEditorRulesTest {

    private val validDraft = DhikrDraft(
        arabicText = "نص",
        transliteration = null,
        bengaliMeaning = null,
        notes = null,
        plannedRepeatCount = 3,
        occasionType = OccasionType.ANYTIME,
    )

    @Test
    fun `valid draft is accepted`() {
        assertTrue(DhikrEditorRules.checkCreate(validDraft) == EditorialCheck.Ok)
    }

    @Test
    fun `blank arabic text is rejected`() {
        val result = DhikrEditorRules.checkCreate(validDraft.copy(arabicText = "   "))
        assertTrue(result is EditorialCheck.Rejected)
        assertEquals(1, (result as EditorialCheck.Rejected).reasons.size)
    }

    @Test
    fun `zero or negative repetition is rejected`() {
        assertTrue(DhikrEditorRules.checkCreate(validDraft.copy(plannedRepeatCount = 0)) is EditorialCheck.Rejected)
        assertTrue(DhikrEditorRules.checkCreate(validDraft.copy(plannedRepeatCount = -3)) is EditorialCheck.Rejected)
    }

    @Test
    fun `absurd repetition is rejected`() {
        val result = DhikrEditorRules.checkCreate(validDraft.copy(plannedRepeatCount = DhikrEditorRules.MAX_REPEAT_COUNT + 1))
        assertTrue(result is EditorialCheck.Rejected)
    }

    @Test
    fun `null repetition is allowed`() {
        assertEquals(EditorialCheck.Ok, DhikrEditorRules.checkCreate(validDraft.copy(plannedRepeatCount = null)))
    }

    @Test
    fun `wording is editable only for user created content`() {
        assertFalse(DhikrEditorRules.canEditWording(ContentClass.VERIFIED_CURATED))
        assertFalse(DhikrEditorRules.canEditWording(ContentClass.NEEDS_REVIEW))
        assertTrue(DhikrEditorRules.canEditWording(ContentClass.USER_CREATED))
    }

    @Test
    fun `user entries are always created with user-created origin`() {
        assertEquals(
            com.qalb.azkar.domain.model.ContentOrigin.USER_CREATED,
            DhikrEditorRules.originForUserEntry(),
        )
    }
}
