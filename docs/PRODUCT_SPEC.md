# Qalb — Product Specification

## 1. Purpose
Qalb helps a Muslim user create a repeatable daily adhkar routine, browse carefully referenced adhkar suggestions, track completion by occasion, and count tasbih. The app should reduce missed adhkar without confusing general time windows with prayer-specific practices.

## 2. Target user and locale
- Initial user: an individual user in Bangladesh.
- Primary UI language: Bengali.
- Content languages: Arabic text, Bengali transliteration where available, and Bengali meaning.
- Design priority: legibility, calm visual design, low friction, accessibility, and offline operation.

## 3. MVP goals
1. Daily dashboard grouped by occasion.
2. Source-referenced curated adhkar library.
3. User can add, edit, reorder, favourite, and remove personal adhkar.
4. Scheduling distinguishes broad time windows from specific prayer triggers.
5. Per-occurrence completion tracking.
6. Persistent tasbih counter with target, undo, reset confirmation, optional vibration, and presets.
7. Local preferences, light/dark theme, adjustable Arabic and Bengali text size.
8. Export/import personal data with validation and clear confirmation.
9. Automated tests, lint, and debug APK artifact via GitHub Actions.

## 4. Explicitly out of scope for MVP
- User accounts, remote database, or cross-device sync.
- Advertising, analytics, subscriptions, or tracking.
- LLM-generated religious suggestions or fatwas.
- Automatic prayer-time/location calculations and prayer notifications.
- Automatic Play Store publishing.
- Social features, leaderboards, streak pressure, and gamification.

## 5. Core screens
- Today: occasion groups, progress, completion controls, and links to item details.
- Library: search, categories, source information, and verification status.
- Dhikr detail/editor: Arabic, transliteration, translation, repetition, occasions, sources, notes, and custom/curated status.
- Collections: user-defined groups and ordering.
- Tasbih: preset, count, target, increment, undo, reset, optional vibration.
- Settings: language/display preferences, theme, export/import, and about/content notes.

## 6. User stories
- As a user, I can see general morning adhkar separately from adhkar that must be presented after Fajr.
- As a user, I can see every-fard-prayer adhkar attached to the relevant prayer session.
- As a user, I can create a custom adhkar entry without it being falsely labelled as a hadith.
- As a user, I can add the same dhikr to multiple collections without duplicating its source record.
- As a user, I can complete one occasion without accidentally completing another occurrence.
- As a user, I can use a tasbih counter offline and recover its count after restarting the app.
- As a user, I can export a backup and restore it after reviewing the import summary.

## 7. Acceptance criteria
- App works without network access after installation.
- Arabic shaping and right-to-left layout work on supported Android devices.
- Curated records show a source reference and a review status; unreviewed records are clearly labelled.
- No generated religious content is presented as authenticated by default.
- Occurrences have stable IDs; completion is attached to an occurrence and date/session.
- Counter increment, undo, target completion, reset, and persistence have tests.
- Import handles malformed files and duplicate IDs without silent data loss.
- Unit tests, lint, and debug build pass locally and in CI, or any environment blocker is explicitly documented.

## 8. Product principles
- Source integrity before content quantity.
- Personalisation without altering a source text silently.
- Clear distinction between a time window and a prayer-specific trigger.
- Private by default; local storage by default.
- No guilt-oriented streak design or claims that the app itself guarantees acceptance of worship.
