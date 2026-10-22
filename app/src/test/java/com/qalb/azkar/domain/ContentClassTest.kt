package com.qalb.azkar.domain

import com.qalb.azkar.domain.model.ContentClass
import com.qalb.azkar.domain.model.ContentOrigin
import com.qalb.azkar.domain.model.ReviewStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The classification rules are the enforcement point of the religious-content
 * policy (docs/AZKAR_CONTENT_POLICY.md); every rule is exercised here.
 */
class ContentClassTest {

    @Test
    fun `curated plus verified is verified curated`() {
        val contentClass = ContentClass.from(ContentOrigin.CURATED, ReviewStatus.VERIFIED)
        assertEquals(ContentClass.VERIFIED_CURATED, contentClass)
        assertTrue(contentClass.isCuratedWording)
    }

    @Test
    fun `curated plus needs-review is needs review`() {
        val contentClass = ContentClass.from(ContentOrigin.CURATED, ReviewStatus.NEEDS_REVIEW)
        assertEquals(ContentClass.NEEDS_REVIEW, contentClass)
        assertTrue(contentClass.isCuratedWording)
    }

    @Test
    fun `user content can never become verified even if status says so`() {
        // A corrupt write trying to pass user content off as VERIFIED must not work.
        val contentClass = ContentClass.from(ContentOrigin.USER_CREATED, ReviewStatus.VERIFIED)
        assertEquals(ContentClass.USER_CREATED, contentClass)
        assertFalse(contentClass.isCuratedWording)
    }

    @Test
    fun `user content with user-content status is user created`() {
        val contentClass = ContentClass.from(ContentOrigin.USER_CREATED, ReviewStatus.USER_CONTENT)
        assertEquals(ContentClass.USER_CREATED, contentClass)
    }

    @Test
    fun `enum parsers reject unknown names`() {
        assertEquals(null, ContentOrigin.fromNameOrNull("IMPORTED"))
        assertEquals(null, ReviewStatus.fromNameOrNull("SUNNAH"))
        assertEquals(ContentOrigin.CURATED, ContentOrigin.fromNameOrNull("CURATED"))
        assertEquals(ReviewStatus.USER_CONTENT, ReviewStatus.fromNameOrNull("USER_CONTENT"))
    }
}
