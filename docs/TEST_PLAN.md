# Qalb — Test Plan

## Unit tests
### Scheduling
- General morning occurrence appears only under the chosen morning rule.
- Prayer-triggered occurrence appears under its linked prayer event, not just because the current time is morning/evening.
- Same Dhikr with two Occurrence IDs keeps two independent completion states.
- Required repetition count is specific to an occurrence.
- Disabled occurrence is excluded.
- Local-date/session reset follows documented semantics.

### Tasbih
- Increment increases count exactly once.
- Undo reverses the last valid increment.
- Count never becomes negative.
- Reaching target updates target state once.
- Reset clears the expected state only after confirmation at the UI level.
- State is restored after repository/database recreation.

### Data and import/export
- Malformed export fails safely with a clear result.
- Duplicate IDs and version mismatches are handled intentionally.
- User-created entries stay user-created after import.
- Seed-data updates do not erase custom changes.
- Room migrations preserve existing user data.

## Milestone 3 (library) — implemented test coverage
Automated (see `app/src/test/java/com/qalb/azkar/`):
- `domain/ContentClassTest` — user content can never be classified as verified; curated+VERIFIED ⇔ verified-curated; unknown enum names rejected at parse sites.
- `domain/editor/DhikrEditorRulesTest` — Arabic text required; repetition 0/negative/oversized rejected; missing repetition allowed; wording editable only for USER_CREATED.
- `domain/seed/SeedMergeTest` — insert-only filtering; duplicate seed IDs; VERIFIED curated row without a source; verified source without a review date; schema version floor.
- `data/seed/SeedJsonParserTest` — bundled real asset parses & validates cleanly; unknown/missing fields and malformed JSON fail loudly; null handling.
- `data/SeedRepositoryTest` — bundled seed applies exactly once (seed_state); rows with existing seed IDs are never overwritten; invalid/generation-newer files refuse with zero writes; verified seed content classifies as verified-curated.
- `data/LibraryPersistenceTest` — personal CRUD; classification `USER_CREATED`; curated wording/edit/delete refused through the repository; personal note + favourite allowed on curated rows; corruption falls back to NEEDS_REVIEW; SQL search across Arabic/Bengali/transliteration; filter chips; collection create/duplicate-refuse/remove; up/down reorder with contiguous sort orders; moving beyond bounds is a no-op; collection deletion spares dhikr rows; dhikr deletion cascades source refs and collection items; data survives a database reopen.

Deferred (later milestones, per plan): scheduling/occurrence tests, tasbih tests, migration tests (none needed before schema v2).

## UI tests
- Navigation between Today, Library, Tasbih, and Settings.
- Arabic and Bengali text visible and readable.
- Add/edit/remove custom dhikr flow.
- Completion progress changes correctly and survives screen recreation.
- Tasbih increment, undo, and reset flow.
- Import/export success and error messages.

## Manual device checklist
- Test at least one emulator or physical device.
- Test app restart and force-stop/relaunch.
- Test offline mode.
- Test light and dark themes, small and large text, Arabic RTL display, and low-width screen.
- Confirm vibration preference is respected.
- Confirm no permission is requested unless a documented feature needs it.

## CI checks
- Unit tests.
- Android lint.
- Debug APK assembly.
- Upload debug APK as an artifact only after the build is successful.


## Execution environment
All Android unit tests, lint, and APK builds run on GitHub Actions because Android Studio/JDK/SDK/Gradle are not installed locally. Local verification is limited to reviewing files and Git diffs. A successful Actions run must be observed before marking automated checks PASS; physical-device behavior must be tested separately.
