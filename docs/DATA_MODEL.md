# Qalb — Data Model Proposal

This is a logical model, not a demand to implement every table on day one. Have OpenCode inspect it and propose the simplest Room schema that preserves the semantics.

## 1. Dhikr
Represents the core text/content record.
- `id`: stable string/UUID primary key.
- `arabicText`: original Arabic text.
- `transliteration`: optional.
- `bengaliMeaning`: optional/required for curated content when available.
- `notes`: optional explanatory note.
- `origin`: `CURATED` or `USER_CREATED`.
- `reviewStatus`: for example `VERIFIED`, `NEEDS_REVIEW`, or `USER_CONTENT`; do not imply that a status alone replaces scholarly review.
- `isFavourite`: personal setting if favourites are tied to a record.
- created/updated timestamps.

## 2. DhikrOccurrence
Represents one occasion on which the dhikr is presented.
- `id`: stable occurrence ID.
- `dhikrId`: foreign key.
- `occasionType`: e.g. `MORNING_WINDOW`, `EVENING_WINDOW`, `AFTER_EACH_FARD`, `AFTER_FAJR`, `AFTER_MAGHRIB`, `BEFORE_SLEEP`, `CUSTOM`.
- `prayerScope`: optional enum for prayer-specific occurrences.
- `timingRuleId` or documented rule enum: avoid free-form rules that cannot be tested.
- `requiredCount`: number required for this particular occurrence.
- `sortOrder`, `enabled`, and optional collection membership.
- Any source note explaining why the occasion applies.

## 3. SourceReference
- `id`, `dhikrId` or link table if one dhikr can have multiple sources.
- `sourceType`: Quran, hadith collection, scholarly reference, or other documented source.
- `sourceName`, `referenceText`, `url` if appropriate, `gradingText` where relevant, `reviewStatus`, `reviewNote`, `reviewedAt`.
- Never infer or fabricate source details when missing.

## 4. Collection and CollectionItem
- `Collection`: ID, name, description, system/user-created marker, timestamps.
- `CollectionItem`: collection ID, dhikr ID or occurrence ID (decide intentionally based on product behavior), sort order, enabled status.
- Avoid storing a second copied Arabic text just because a dhikr is added to another collection.

## 5. DhikrCompletion
Represents completion for a specific occurrence, not only for a text ID.
- `id`.
- `occurrenceId`.
- `localDate` or a documented session identifier.
- `completedCount` if partial progress is supported.
- `isComplete`, `completedAt`.
- Unique constraint should prevent accidental duplicate completion records for the same occurrence/session.
- Decide and document whether daily progress resets by local date, and how a manual reset behaves.

## 6. TasbihPreset and TasbihSession
Preset:
- ID, name, optional linked Dhikr ID, default target, vibration preference if per-preset.

Session/state:
- preset ID, current count, target, updated time, optional session history.
- Persist current state before or promptly after visible state changes.
- If detailed event history is used, represent increment/undo/reset events carefully; do not overbuild the MVP.

## Key design decisions to test
- One Dhikr may have multiple Occurrence records.
- Completing one Occurrence cannot complete another Occurrence merely because they share a Dhikr ID.
- A required count belongs to the occurrence if it varies by occasion.
- User custom content must not be transformed into curated content on import.
- Seed updates must not silently destroy local edits.
