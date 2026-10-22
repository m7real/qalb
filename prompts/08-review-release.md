# Phase 8 — Review, Hardening, and MVP Readiness (Plan review first, then Build approved fixes)

First use Plan mode to perform a read-only audit against `AGENTS.md`, the relevant `docs/` files, and the MVP checklist in `README-START-HERE.md`.

Review:
- Separation of time-window and prayer-triggered occasions.
- Per-occurrence completion correctness and tasbih persistence.
- Arabic RTL rendering and Bengali typography.
- Accessibility, touch targets, screen sizes, loading/empty/error states.
- Room migrations and seed update behavior.
- Backup/restore safety.
- Meaningful unit/UI test coverage and edge cases.
- GitHub Actions permissions, action versions, SDK/Gradle compatibility, and artifact paths.
- Religious source status and distinction between curated and personal content.
- Unnecessary permissions, networking, and dependencies.

Return findings ranked by severity with file references and minimal fixes. Do not edit during the audit. After I review the findings, use Build to fix one logical group at a time.

Environment constraint: no Android Studio/JDK/SDK/Gradle locally. Never run local Android build commands. For every fix, use GitHub Actions after push to verify tests, lint, and APK build. Do not claim a cloud check passed without a green Actions run.

Do not publish to Play Store, generate release signing keys, add tracking, or add new scope. Finish with a truthful PASS / FAIL / NOT VERIFIED checklist, exact CI run reference/result provided by me, and remaining phone checks.
