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

## Applied in Milestone 3 — record of choices (2026-10-22)

Schema version 1 (dump committed under `app/schemas/`). Migrations written
against those dumps before any version bump.

Decisions for the open design questions above:

- **CollectionItem FK target (§4):** the FK points at the **Dhikr row**, not the
  occurrence. Collections are ordered presentations of content; occurrences are
  scheduling, which arrives in a later milestone. Duplicate membership rows are
  prevented by a unique `(collection_id, dhikr_id)` index and refused loudly by
  the repository. No copied text lives in collection items.
- **Occasion/repetition on Dhikr (§2):** until the scheduling milestone, the
  creator of a personal entry records `occasion_type` (closed vocabulary:
  ANYTIME, MORNING_WINDOW, EVENING_WINDOW, AFTER_EACH_FARD, AFTER_FAJR,
  AFTER_MAGHRIB, BEFORE_SLEEP, CUSTOM) and `planned_repeat_count` as
  *personal defaults* on the Dhikr row itself. These are never presented as
  established worship rules, and no prayer-time permission or binding exists in
  this milestone.
- **Seed merge (§5):** `seed_state` (single row) records the applied seed
  schema version. Seeding is **insert-only**: rows whose ID exists are skipped
  entirely, so a seed update can never rewrite existing wording — verified by
  `SeedRepositoryTest`. Files whose schema version is newer than the app
  supports, or failing structural validation (duplicate IDs, blank text,
  VERIFIED without a source, verified source without a review timestamp), are
  refused before anything is written.
- **Curated rows are immutable in-app:** UI exposes only the personal note and
  the favourite flag on curated content; wording edits are repository-refused
  for anything not USER_CREATED. Corrupt enum values fall back to
  NEEDS_REVIEW, so damaged rows can never display as verified.
- **Starter data:** because no qualified reviewer has yet verified wording,
  the bundled `seed/azkar-library-v1.json` contains ONLY clearly labelled
  placeholder fixtures (`sample-placeholder-1..3`, all NEEDS_REVIEW, no
  occurrences, no repetition, no source rows). No real adhkar text is claimed.
