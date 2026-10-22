# Phase 6 — Tasbih Counter (use Build)


Environment rule: do not run local Gradle/Android SDK build commands. All compile/test/lint/APK checks must be verified by GitHub Actions. Never claim the build passed before a successful cloud run.
Read `AGENTS.md`, `docs/PRODUCT_SPEC.md`, `docs/APP_ARCHITECTURE.md`, `docs/DATA_MODEL.md`, and `docs/TEST_PLAN.md`.

Implement a reliable offline tasbih counter without rewriting unrelated features.

Requirements:
- Prominent tap target and clearly visible count/target.
- User can create/select named presets and configure the target.
- Increment is processed once per tap; avoid losing counts during rapid taps.
- Add undo for the latest valid increment; prevent negative counts.
- Confirm destructive reset when the count is nonzero.
- Persist current state locally so it survives app restart/process recreation.
- Add optional vibration with a setting to disable it; request no unrelated permissions.
- Keep tasbih counter session state separate from adhkar occurrence completion state.
- Optional history should remain simple; do not create unnecessary analytics.

Add tests for increment, undo, target reached, reset state, persistence, and invalid states. Do not run local Gradle/Android SDK commands. Report the GitHub Actions tasks and device test steps; do not claim PASS until CI succeeds, then stop.
