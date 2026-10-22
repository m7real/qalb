package com.qalb.azkar.domain.model

/**
 * Domain projection of a Dhikr record. Room entities live in data/local;
 * feature screens see only this type.
 */
data class Dhikr(
    val id: String,
    val arabicText: String,
    val transliteration: String?,
    val bengaliMeaning: String?,
    val notes: String?,
    val contentClass: ContentClass,
    val isFavourite: Boolean,
    /** Personal/editor default repetition count; no worship claim is attached to it. */
    val plannedRepeatCount: Int?,
    val occasionType: OccasionType?,
    val createdAt: Long,
    val updatedAt: Long,
)

/** Source reference attached to a Dhikr (docs/DATA_MODEL.md §3). Never fabricated. */
data class SourceReference(
    val id: String,
    val dhikrId: String,
    val sourceType: String,
    val sourceName: String,
    val referenceText: String?,
    val url: String?,
    val gradingText: String?,
    val reviewStatus: ReviewStatus,
    val reviewNote: String?,
    val reviewedAt: Long?,
)

/** A user-defined ordered grouping of dhikr records (docs/DATA_MODEL.md §4). */
data class Collection(
    val id: String,
    val name: String,
    val description: String?,
    val createdAt: Long,
)

/** A membership row; ordering is collection-local. Text is NOT copied here. */
data class CollectionItem(
    val id: String,
    val collectionId: String,
    val dhikrId: String,
    val sortOrder: Int,
    val enabled: Boolean,
)

/** A collection row together with the Dhikr it currently presents. */
data class CollectionDhikr(
    val item: CollectionItem,
    val dhikr: Dhikr,
)
