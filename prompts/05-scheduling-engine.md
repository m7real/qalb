# Phase 5 — Occasion Scheduling and Completion (use Build)


Environment rule: do not run local Gradle/Android SDK build commands. All compile/test/lint/APK checks must be verified by GitHub Actions. Never claim the build passed before a successful cloud run.
Read `AGENTS.md`, `docs/PRODUCT_SPEC.md`, `docs/APP_ARCHITECTURE.md`, `docs/DATA_MODEL.md`, `docs/AZKAR_CONTENT_POLICY.md`, and `docs/TEST_PLAN.md`.

Implement the scheduling domain model and Today dashboard only. Do not change religious content or invent source rulings.

Requirements:
- Represent broad time windows (e.g. general morning/evening) separately from prayer-triggered occasions (e.g. after Fajr, after Maghrib, after every obligatory prayer).
- Use stable DhikrOccurrence identities and attach required repetition count to the occurrence when it can vary.
- Create a testable domain component/use case that groups enabled occurrences into the correct dashboard sections.
- Keep schedule rules out of Composables.
- Track completion per occurrence and documented day/session, not only by Dhikr ID.
- Completing one occurrence must never automatically complete a separate occurrence merely because the Arabic text/Dhikr ID matches.
- Support incomplete/partially completed state only if it can be implemented without ambiguous semantics; otherwise use a clear complete/not-complete MVP.
- Do not add location permission, network service, prayer-time calculations, or notification scheduling.
- Clearly communicate that time-window labels reflect a documented app rule and that disputed interpretations need review.

Add unit tests for every requirement, including duplicate-text/multiple-occurrence cases and local-date/session behavior. Add relevant unit tests, but do not run local Gradle/Android SDK commands. Use GitHub Actions for all tests, lint, and debug APK assembly. Report the CI task names and manual phone checks; do not claim PASS until a successful Actions run is observed. Stop after this milestone.
