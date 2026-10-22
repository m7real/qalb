package com.qalb.azkar.data.local

import com.qalb.azkar.domain.model.Collection
import com.qalb.azkar.domain.model.CollectionItem
import com.qalb.azkar.domain.model.ContentClass
import com.qalb.azkar.domain.model.ContentOrigin
import com.qalb.azkar.domain.model.Dhikr
import com.qalb.azkar.domain.model.OccasionType
import com.qalb.azkar.domain.model.ReviewStatus
import com.qalb.azkar.domain.model.SourceReference

/**
 * Entity <-> domain mapping. Corrupt/unknown stored enum names can only come
 * from external tampering or a future migration bug; the safe fallback is
 * NEEDS_REVIEW, which can never make anything look verified.
 */
fun DhikrEntity.toDomain(): Dhikr = Dhikr(
    id = id,
    arabicText = arabicText,
    transliteration = transliteration,
    bengaliMeaning = bengaliMeaning,
    notes = notes,
    contentClass = resolveClass(origin, reviewStatus),
    isFavourite = isFavourite,
    plannedRepeatCount = plannedRepeatCount,
    occasionType = OccasionType.fromNameOrNull(occasionType),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun SourceReferenceEntity.toDomain(): SourceReference = SourceReference(
    id = id,
    dhikrId = dhikrId,
    sourceType = sourceType,
    sourceName = sourceName,
    referenceText = referenceText,
    url = url,
    gradingText = gradingText,
    reviewStatus = ReviewStatus.fromNameOrNull(reviewStatus) ?: ReviewStatus.NEEDS_REVIEW,
    reviewNote = reviewNote,
    reviewedAt = reviewedAt,
)

fun CollectionEntity.toDomain(): Collection = Collection(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
)

fun CollectionItemEntity.toDomain(): CollectionItem = CollectionItem(
    id = id,
    collectionId = collectionId,
    dhikrId = dhikrId,
    sortOrder = sortOrder,
    enabled = enabled,
)

private fun resolveClass(origin: String, reviewStatus: String): ContentClass {
    val originValue = ContentOrigin.fromNameOrNull(origin)
    val statusValue = ReviewStatus.fromNameOrNull(reviewStatus)
    return if (originValue == null || statusValue == null) {
        ContentClass.NEEDS_REVIEW
    } else {
        ContentClass.from(originValue, statusValue)
    }
}
