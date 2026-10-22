# Qalb — Implementation Roadmap (GitHub Actions builds only)

## Milestone 0 — Specification and cloud-only workflow plan
Deliverables: reviewed product scope, architecture proposal, data model, content policy, UI rules, test plan, `AGENTS.md`, and a CI-only build strategy.
Exit criteria: no unresolved decisions blocking the scaffold; disputed religious timing/fiqh rules are explicitly marked for content review.

## Milestone 1 — Native project scaffold
Deliverables: Kotlin/Compose project source and Gradle config created without Android Studio; theme, basic navigation, manifest, minimal tests, `.gitignore`.
Exit criteria: source has been pushed and first GitHub Actions workflow can compile it; never infer success from local editing.

## Milestone 2 — GitHub Actions baseline (before feature work)
Deliverables: workflow provisions compatible JDK, Android SDK, and explicit Gradle version; runs unit tests, lint, debug APK assemble; uploads APK artifact.
Exit criteria: at least one real green GitHub Actions run and downloadable debug APK.

## Milestone 3 — Library and personal content
Deliverables: Room persistence, seed loading and validation, library/search/detail, custom add/edit/delete/reorder, collections.
Exit criteria: data survives restart; curated and user-authored content are visibly distinct; CI repository/content tests pass.

## Milestone 4 — Scheduling and completion
Deliverables: occurrence domain model, occasion groups, completion tracking, per-occurrence progress, morning/evening/prayer-specific separation.
Exit criteria: scheduling/completion tests pass in CI; no inferred prayer binding; rule behavior documented.

## Milestone 5 — Tasbih
Deliverables: persistent counter, presets, target, undo, reset confirmation, optional vibration, simple history if needed.
Exit criteria: counter state survives restart; counter tests pass in CI.

## Milestone 6 — Settings and data portability
Deliverables: appearance settings, text scaling, export/import validation, restore confirmation.
Exit criteria: settings persist; malformed imports fail safely; backup/restore tests pass in CI and manual phone checks are completed.

## Milestone 7 — Final audit and release readiness
Deliverables: GitHub Actions stability, accessibility and RTL fixes, religious-content audit, security review, debug APK artifact.
Exit criteria: green CI; APK installs on a physical phone; no secrets or signing keys committed. Play Store signing/publishing is out of MVP scope.

## Deferred
Prayer-time calculation, reminders, cloud sync, accounts, runtime AI religious recommendations, ads/analytics, and Play Store automation. Each requires separate scope/review.
