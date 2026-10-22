# Diagnose and Fix a GitHub Actions Failure (use Build only after diagnosis)

I will paste the real log from a failed GitHub Actions run. Read `AGENTS.md` and the relevant project/workflow files first.

Constraints:
- Do not run local Gradle, JDK, Android SDK, or emulator commands.
- Identify the earliest meaningful error and root cause before editing.
- Distinguish dependency/network failures, SDK-package mismatch, Java/Gradle/AGP incompatibility, Kotlin compile errors, test failures, lint failures, and artifact-path failures.
- Never delete, disable, skip, or weaken tests/lint/build steps just to make CI green.
- Propose the smallest safe fix. If it is obvious and tightly scoped, implement it; for broad changes, explain the proposed patch first.
- Inspect the diff and check available non-build validations, including `git diff --check`.
- Do not claim success until the user commits/pushes and a new GitHub Actions run passes.

At the end, report root cause, changed files, the exact commands the user should use to commit/push, and which Actions run to check next.
