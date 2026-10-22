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
