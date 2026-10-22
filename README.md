# Qalb

Offline-first native Android app for personal adhkar routines, a source-referenced
adhkar library, and a persistent tasbih counter. Primary UI language: Bengali.
See `docs/PRODUCT_SPEC.md` for scope. **This repository only contains religious
content from explicitly curated records — see `docs/AZKAR_CONTENT_POLICY.md`.**

## ☁️ Cloud-only build rule (read this first)

**There is no Android Studio, Android SDK, JDK, Gradle, or emulator installed on
the maintainer's computer — and none may be installed. C: drive space is
constrained.**

- Android compilation, unit tests, lint, and APK assembly run **only on GitHub
  Actions** (the `.github/workflows/android-ci.yml` workflow is created in the
  next phase).
- Do not run `gradle`, `./gradlew`, `sdkmanager`, or any emulator command.
- The project intentionally has **no Gradle wrapper**: the CI workflow provisions
  an explicit pinned Gradle version via `gradle/actions/setup-gradle` and invokes
  `gradle` on the GitHub-hosted runner.
- A valid change sequence is always: edit → `git diff --check` → commit → push →
  open GitHub → **Actions** → latest run. **Success is only ever claimed after a
  green Actions run.** Never infer it from local editing.
- Download the debug APK from a successful run's **Artifacts** section and test it
  on a real Android phone (see `docs/TEST_PLAN.md` → Manual device checklist).

## 🔒 Version registry (single source of truth)

Real machine-readable registry: [`gradle/libs.versions.toml`](gradle/libs.versions.toml).
This table mirrors it. Any change touches **all three** places (catalog, README,
CI workflow) in one commit and must be confirmed by a green Actions run.

| Tool / setting | Version | Reason recorded |
| --- | --- | --- |
| JDK (CI) | 17 (Temurin) | AGP 8.13 minimum/standard JDK |
| Android Gradle Plugin | 8.13.2 | Last stable 8.x; avoids the AGP 9 new-DSL/built-in-Kotlin transition; Kotlin 2.3 officially supported (R8 8.13.19) |
| Gradle (CI pins 8.14.3) | 8.14.3 | AGP 8.13 needs Gradle ≥ 8.13; AGP 8.x does not support Gradle 9; KGP 2.3 works with Gradle up to 9.0 |
| Kotlin / Compose compiler plugin | 2.3.21 | Vendor-matched pair with AGP 8.13.2 (per android.com Kotlin-support table); Kotlin 2.4 would require AGP 9.x-era R8 |
| Compose BOM | 2026.06.01 | Maps compose 1.11.4 / material3 1.4.0 / ripple 1.11.4 — newest generation whose AAR metadata accepts compileSdk 36 + AGP 8.13 (compose 1.12.x requires API 37 + AGP 9.1) |
| androidx navigation-compose | 2.9.8 | Newest navigation accepting compileSdk 36 + AGP 8.13 (2.10.0 requires API 37 + AGP 9.1, verified via AAR metadata) |
| androidx activity-compose | 1.12.4 | Covers compileSdk 36; newer 1.13.0 drags lifecycle 2.11.0 transitively (AGP 9.1 requirement) |
| androidx core-ktx | 1.18.0 | Newest core-ktx accepting compileSdk 36 + AGP 8.13 (1.19.x requires API 37 + AGP 9.1) |
| androidx lifecycle-runtime-ktx | 2.10.0 | Newest stable lifecycle line accepting AGP ≤ 8.13 (2.11.0 requires AGP 9.1); same version serves lifecycle-runtime-compose + lifecycle-viewmodel-compose |
| androidx Room (+ room-ktx, room-compiler via KSP) | 2.8.5 | Newest stable Room release (analysis verified 2026-10-22); works with compileSdk 36 / AGP 8.13 |
| KSP (Room codegen) | 2.3.12 | New standalone KSP versioning on the Kotlin 2.3 line; must track the Kotlin 2.3.x used |
| Robolectric (unit tests) | 4.17 | Newest stable; enables real Room/asset tests on JVM runners (android-all fetched at test time) |
| androidx test-core (tests) | 1.7.0 | Latest stable (1.8.0 is alpha — avoided per stable-only policy) |
| compileSdk / targetSdk | 36 | AGP 8.13 supports up to API 36.1; matches current Play target requirement |
| minSdk | 24 | Broad device coverage for the target user profile |
| build-tools (CI installs same) | 35.0.0 | AGP 8.13 documented default build-tools |
| JUnit | 4.13.2 | Stable unit-test framework for JVM tests |

Library versions added with Milestone 3 (Room / KSP / Robolectric / test-core)
are recorded above; future milestones append new rows here as they are needed.

## ☁️ How to verify a change — the only build loop you need

Your computer never builds Android. GitHub Actions does:

1. **Local (no Android tools involved):** open the changed files, review them,
   run `git diff --check`, and read the diff carefully. This is *static
   inspection only* — it never proves the app compiles or the tests pass.
2. **Commit and push** (see exact commands below). Pushing to `main` (or opening
   a pull request that targets `main`) triggers **android-ci** in the Actions tab.
3. **Inspect the run:** open your repository → **Actions** → the latest
   `android-ci` run. Green means unit tests (`:app:testDebugUnitTest`), Android
   lint (`:app:lintDebug`), and debug APK assembly (`:app:assembleDebug`) all
   passed in the cloud. **Only a completed green run counts as PASS.** A local
   review is never acceptable evidence on its own.
4. **If red:** open the first failed step (Unit tests / Android lint / Assemble),
   read the tail of the log for the first real error, and paste it into OpenCode
   along with `prompts/09-fix-ci-failure.md`. Do not delete tests to get green.
5. **Download and install the APK:** on a green run, scroll to **Artifacts** →
   download `qalb-debug-apk` → unzip (GitHub wraps artifacts in a ZIP) → move the
   APK to your Android phone → install it (allow "install from this source" if
   asked) → test on the device per `docs/TEST_PLAN.md` §Manual device checklist.
6. Repeat: edit locally → push → read the run. That is the whole loop.

CI never signs a release APK, never publishes to Play Store, and needs no
secrets. Workflow permissions are minimal (`contents: read`), no claim of
success is made until a green run is observed, and a green CI run still does
not replace physical-device testing.

### Gradle wrapper policy and version consistency

- The repo intentionally has **no Gradle wrapper**. CI provisions Gradle with
  `gradle/actions/setup-gradle` using the version pinned in
  `gradle/libs.versions.toml` (`gradlePin`). If `gradlew` ever appears in the
  repo root, CI automatically switches to `./gradlew` (checksum-validated) in
  the same run — one consistent strategy either way.
- The CI sync-check step fails early if the workflow's SDK package pins
  (`platforms;android-36`, `build-tools;35.0.0`) ever drift from the version
  catalog, which is also what `app/build.gradle.kts` reads.
- Version changes always touch: `gradle/libs.versions.toml`, the workflow, and
  the table above — in one commit — validated by a green Actions run.
- Action major tags were verified against official release pages on
  2026-10-22 (`checkout` v7, `setup-java` v6, `setup-android` v4,
  `setup-gradle` v6, `upload-artifact` v7). Prefer updating major tags via
  verified release notes; commit-SHA pinning is not used because SHAs cannot
  be reliably verified offline in this workflow.

## Module layout

```text
app/
└── src/main/java/com/qalb/azkar/
    ├── MainActivity.kt        # single Activity, Compose host
    ├── feature/               # today | library | tasbih | settings (placeholders in scaffold)
    ├── navigation/            # bottom-bar NavHost
    ├── ui/theme/              # Material 3 brand palette (light + dark)
    ├── data/                  # (Room arrives in Milestone 3)
    └── domain/                # (scheduling/completion rules arrive in Milestone 4)
```

`data/` and `domain/` directories are created together with their first real
files; nothing fictional is committed.

## Commit and push flow

```bash
git status          # confirm only intended files changed
git diff --check    # whitespace audit
git add .
git commit -m "feat: <short-change-description>"
git push
```

Then follow **☁️ How to verify a change** above.

## What is deliberately NOT implemented yet

Adhkar library/content, scheduling rules, tasbih behavior, export/restore,
religious seed data, remote sync, analytics. Each arrives in its roadmap
milestone (`docs/ROADMAP.md`).

## Next step in the project flow

Phase 2 (scaffold) and Phase 3 (this CI workflow) are in place but **not yet
verified by a cloud build**. Push both to GitHub, confirm the first green
`android-ci` run plus the `qalb-debug-apk` artifact, then continue with
`prompts/04-azkar-data-and-library.md`.
