# Phase 4 — Adhkar Library and Personal Data (use Build)


Environment rule: do not run local Gradle/Android SDK build commands. All compile/test/lint/APK checks must be verified by GitHub Actions. Never claim the build passed before a successful cloud run.
Read `AGENTS.md`, `docs/PRODUCT_SPEC.md`, `docs/APP_ARCHITECTURE.md`, `docs/DATA_MODEL.md`, and `docs/AZKAR_CONTENT_POLICY.md`.

Inspect the existing app and implement only the library/personal-content milestone.

Requirements:
- Add Room entities, DAOs, database, repositories, and migrations as needed for Dhikr, source references, collections, and collection items.
- Include a small, clearly labelled starter dataset only if every curated item's exact wording, reference, repetition, and occasion can be verified. If source verification is not possible, create empty/example fixtures marked `NEEDS_REVIEW` or clearly labelled sample content; never fabricate hadith references.
- Provide a searchable library and a dhikr detail screen.
- Allow a user to create, edit, and delete user-created entries; add favourites and collections if this fits the current schema.
- Let the user set Arabic text, optional transliteration, Bengali meaning, notes, repetition count, and occasion metadata.
- Visibly distinguish reviewed curated entries from unreviewed/imported/user-created content.
- Keep source and religious-content rules out of UI-only code.
- Ensure seed updates do not silently overwrite the user's edited content.
- Keep all core library features usable offline.

Add unit/repository tests for persistence, CRUD, collection ordering, content classification, and seed-data safety. Add/update relevant tests. Do not run local Gradle/Android SDK commands. Push the changes and use GitHub Actions for tests, lint, and debug APK assembly; do not claim PASS until that run succeeds. Fix failures shown by the real CI logs.

Do not implement final scheduling/completion logic or tasbih in this phase. Report exact verification commands and manual test steps, then stop.
