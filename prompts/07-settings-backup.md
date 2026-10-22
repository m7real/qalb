# Phase 7 — Settings and Local Backup/Restore (use Build)


Environment rule: do not run local Gradle/Android SDK build commands. All compile/test/lint/APK checks must be verified by GitHub Actions. Never claim the build passed before a successful cloud run.
Read `AGENTS.md`, `docs/PRODUCT_SPEC.md`, `docs/APP_ARCHITECTURE.md`, `docs/DATA_MODEL.md`, and `docs/TEST_PLAN.md`.

Implement only the essential settings and data-portability features.

Requirements:
- Use DataStore for simple preferences such as theme, Arabic text scale, Bengali text scale, and vibration preference.
- Keep user data local and make offline behavior explicit.
- Add an explicit export action for personal data and an import/restore flow using Android's appropriate document picker/storage APIs.
- Use a versioned export format and validate file type, schema version, required fields, lengths, and IDs.
- Show a preview/summary and ask for confirmation before import can create duplicates or modify existing data.
- Never silently overwrite the database. Prefer a safe merge policy with clear conflict handling, or explain the supported behavior to the user.
- Do not export secret machine data, SDK paths, or unnecessary device identifiers.
- Handle corrupt files and unsupported versions with helpful errors.

Add tests for malformed files, duplicate IDs, version mismatches, preservation of user-created content, and settings persistence. Do not run local Gradle/Android SDK commands. Use GitHub Actions to verify; report manual phone checks and stop.
