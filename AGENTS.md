# Qalb Project Instructions

## Product
Qalb is an offline-first native Android app for personal adhkar routines, a source-referenced adhkar library, and a persistent tasbih counter. The primary UI supports Bengali and Arabic.

## Developer environment: CI-only Android builds
The maintainer is new to OpenCode and does not have Android Studio; the C: drive is space-constrained. Source editing happens locally with OpenCode and Git. **All Android compilation, unit tests, Android lint, and APK builds must run on GitHub Actions.**

- Do not ask to install Android Studio, Android SDK, JDK, Gradle, or an emulator locally.
- Do not run `gradle`, `./gradlew`, `sdkmanager`, Android emulator commands, or any local Android build check.
- Do run available non-build checks such as reviewing files, `git diff --check`, and workflow/YAML syntax validation if a validator already exists.
- Do not claim the Android code compiles unless a successful GitHub Actions run verifies it.
- After code changes, tell the maintainer which files changed, what to commit/push, which Actions run to inspect, and which on-phone manual checks remain.
- If CI fails, diagnose the actual log; never remove tests or skip required build tasks merely to get a green run.

## Working approach
- Read `docs/PRODUCT_SPEC.md`, `docs/APP_ARCHITECTURE.md`, and relevant documents before implementing.
- Implement only the requested milestone; inspect current files and `git diff` before broad changes.
- Keep each task small and explain commands simply.
- Do not commit or push without a direct instruction from the maintainer.
- The first app scaffold must be created without Android Studio. Include all source/build configuration required by a native Kotlin + Jetpack Compose Android project.
- Establish the GitHub Actions workflow early, immediately after the first project scaffold and before implementing the main features.

## Technical defaults
- Kotlin, Jetpack Compose, Material 3.
- Simple MVVM/repository boundaries; Room for structured local data; DataStore for preferences.
- Coroutines/Flow and lifecycle-aware state collection.
- Offline-first; no account, backend, analytics, ads, or network requirement in the MVP.
- Choose mutually compatible stable Android Gradle Plugin, Kotlin plugin, Gradle, JDK, compileSdk, and build-tools versions. Validate compatibility against current official documentation when possible and record chosen versions.
- Since the initial environment has no local Gradle installation, the CI workflow should provision a pinned compatible Gradle version with `gradle/actions/setup-gradle` and can invoke `gradle ...` on the GitHub-hosted runner if a wrapper is not present. If a wrapper is added later, ensure the CI workflow uses it consistently.
- Keep domain rules out of Composables. Handle loading, empty, error, and success states where useful.
- Do not add libraries or architecture complexity without a concrete need.

## Religious-content integrity
- Never invent Quranic wording, hadith wording, citations, hadith numbers, authenticity grades, prescribed repetitions, or timing rules.
- Keep curated source content separate from user-created content and from UI code.
- Store source references and editorial verification status explicitly.
- User-created entries are personal content; never label them authenticated/Sunnah by default.
- Do not label a practice obligatory unless the ruling is established for that exact practice.
- Do not silently resolve disputed Hanafi fiqh/timing questions; document the selected editorial rule, source, and review status.
- User-facing suggestions must be traceable to curated records; no runtime LLM-generated religious content in the MVP.

## Scheduling semantics
- Distinguish broad time windows (for example, general morning adhkar) from prayer-triggered occasions (for example, after Fajr).
- A Dhikr can have multiple occasions; each occurrence/completion needs a clear identity.
- Completing one occurrence must not complete another merely because text/Dhikr ID matches.
- Store repetition count per occurrence if it can vary by context.
- Document time-window interpretation; do not require location or prayer-time permissions for the MVP.

## Tasbih semantics
- Persist current count and preset locally.
- Undo reverses the latest increment safely; never allow negative counts.
- Confirm destructive reset where useful.
- Do not conflate tasbih session state with adhkar occurrence completion.

## Verification and reporting
- Run Android tests, lint, and builds only on GitHub Actions; local environment cannot run them.
- For each change, use the narrow relevant tests + full unit tests + lint + debug APK assemble as CI steps once configured.
- Report the exact cloud workflow/tasks expected, and only state PASS after examining a green Actions run.
- For red CI, request or inspect logs and implement the smallest safe fix; do not weaken tests.
- Always state what needs manual testing on a real Android phone.

## Git and secrets
- Do not commit API keys, passwords, personal access tokens, keystores, signing keys, or machine-specific files.
- Keep `.gitignore` appropriate for Gradle/Android build outputs, local SDK settings, IDE files, and secrets.
- Give GitHub Actions the minimum permissions needed; no release signing or automatic Play Store publishing without a separate explicit request.
