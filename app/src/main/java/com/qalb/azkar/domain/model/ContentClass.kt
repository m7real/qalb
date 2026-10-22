package com.qalb.azkar.domain.model

/**
 * Where a Dhikr record came from (docs/DATA_MODEL.md §1).
 * Stored as strings in Room; parsed here so the domain owns the vocabulary.
 */
enum class ContentOrigin {
    CURATED,
    USER_CREATED,
    ;

    companion object {
        fun fromNameOrNull(name: String?): ContentOrigin? =
            entries.firstOrNull { it.name == name }
    }
}

/**
 * Editorial review status of the CONTENT (docs/AZKAR_CONTENT_POLICY.md).
 * - VERIFIED: entered from an identifiable source AND checked by a competent reviewer.
 * - NEEDS_REVIEW: imported/seeded for personal use until checked.
 * - USER_CONTENT: personal dhikr entered by the user.
 *
 * USER_CONTENT never implies authenticity; VERIFIED is granted only by an
 * explicit editorial process recorded outside the app.
 */
enum class ReviewStatus {
    VERIFIED,
    NEEDS_REVIEW,
    USER_CONTENT,
    ;

    companion object {
        fun fromNameOrNull(name: String?): ReviewStatus? =
            entries.firstOrNull { it.name == name }
    }
}

/**
 * The single well-defined content class surfaced to the UI.
 * Derivation rules (tested in ContentClassTest):
 *  - USER_CREATED rows are USER_CREATED, no matter what any stored status says.
 *  - CURATED + VERIFIED -> VERIFIED_CURATED.
 *  - Anything else -> NEEDS_REVIEW.
 */
enum class ContentClass(val isCuratedWording: Boolean) {
    VERIFIED_CURATED(isCuratedWording = true),
    NEEDS_REVIEW(isCuratedWording = true),
    USER_CREATED(isCuratedWording = false),
    ;

    companion object {
        fun from(origin: ContentOrigin, reviewStatus: ReviewStatus): ContentClass = when {
            origin == ContentOrigin.USER_CREATED -> USER_CREATED
            reviewStatus == ReviewStatus.VERIFIED -> VERIFIED_CURATED
            else -> NEEDS_REVIEW
        }
    }
}
