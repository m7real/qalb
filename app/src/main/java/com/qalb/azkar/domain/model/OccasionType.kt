package com.qalb.azkar.domain.model

/**
 * Occasion vocabulary (docs/DATA_MODEL.md §2). Closed set: adding a value is a
 * code change + test + review note, never a free-form user input.
 *
 * In this milestone the value is stored on the Dhikr row as the user's own
 * intended occasion/metadata; Milestone 5 will materialise per-occurrence rows
 * from it. No prayer-time binding is asserted anywhere (docs/PRODUCT_SPEC.md).
 */
enum class OccasionType {
    ANYTIME,
    MORNING_WINDOW,
    EVENING_WINDOW,
    AFTER_EACH_FARD,
    AFTER_FAJR,
    AFTER_MAGHRIB,
    BEFORE_SLEEP,
    CUSTOM,
    ;

    companion object {
        fun fromNameOrNull(name: String?): OccasionType? =
            entries.firstOrNull { it.name == name }
    }
}
