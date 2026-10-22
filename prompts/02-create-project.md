# Phase 2 — Create the Native Android Project From Scratch (use Build)

Read `AGENTS.md`, `docs/PRODUCT_SPEC.md`, `docs/APP_ARCHITECTURE.md`, `docs/ROADMAP.md`, and `docs/UX_GUIDELINES.md`. Inspect the repository before editing.

Environment constraints: There is no Android Studio, Android SDK, JDK, or Gradle installation on the developer computer. C: drive space is constrained. Do not install tools locally and do not run local Android build commands. All compile/test/lint/APK work happens on GitHub Actions.

Create a minimal but valid native Android project using Kotlin, Jetpack Compose, and Material 3 with application ID `com.qalb.azkar`. This is a from-scratch scaffold; do not assume Android Studio has generated files.

Scope:
- Create the root Gradle settings/build files, app module, manifest, Compose entry activity, theme, and standard source/test directories.
- Choose mutually compatible stable versions for Android Gradle Plugin, Kotlin, Gradle, Java, compileSdk, and build tools. Validate current compatibility using official documentation if web access is available. Record every selected version in README and relevant Gradle/workflow configuration.
- Since local Gradle is unavailable, do not require a local Gradle Wrapper in order to verify the first build. The next phase will create a GitHub Actions workflow that provisions an explicit compatible Gradle distribution on the runner. If wrapper files are easy to include safely, you may add them, but CI must not fail merely because there is no wrapper.
- Add a simple Qalb placeholder home screen and navigation placeholders for Today, Library, Tasbih, and Settings. Do not implement full feature behavior yet.
- Add `.gitignore` excluding build outputs, local SDK properties, IDE machine files, secrets, and signing material.
- Add a short README section with the cloud-only build rule and the GitHub Actions path; no local Android build instructions.
- Add a minimal unit test setup if compatible with the scaffold.

Do not implement the adhkar library, scheduling rules, tasbih behavior, export/restore, remote sync, analytics, or religious seed data yet.

Before stopping, perform only checks that do not require JDK/Gradle/Android SDK (for example, inspect changed files and `git diff --check`). Do not execute `gradle`, `./gradlew`, `sdkmanager`, or emulator commands. Do not claim the app builds yet.

Report changed files, chosen compatibility versions, anything still uncertain, and the exact next step: run the GitHub Actions setup prompt and push so the first cloud build can verify the scaffold. Stop here.
