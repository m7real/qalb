# Phase 3 — GitHub Actions Cloud Build (use Build)

Read `AGENTS.md`, `docs/ROADMAP.md`, `docs/TEST_PLAN.md`, and the actual Gradle project files. Inspect the chosen AGP/Kotlin/Gradle/Java/compileSdk/build-tools versions before creating the workflow.

This project has no Android Studio, Android SDK, JDK, or Gradle locally. **The Android build must happen entirely on GitHub-hosted runners.** Do not instruct me to run a local Android compile/test/lint task.

Create `.github/workflows/android-ci.yml` with these requirements:
- Trigger on pushes and pull requests to `main` (and adapt the branch if the repository uses a different default branch).
- Use supported current stable GitHub Actions versions; prefer official actions. Current candidate major tags as of this project setup are `actions/checkout@v7`, `actions/setup-java@v6`, `android-actions/setup-android@v4`, `gradle/actions/setup-gradle@v6`, and `actions/upload-artifact@v7`. Confirm they are still valid when creating the workflow. Pin full commit SHAs only when they can be verified reliably; document tag/commit update policy.
- Set up a JDK version supported by the selected Android Gradle Plugin.
- Set up the Android SDK on the runner and install the exact platform/build-tools package versions required by `compileSdk` and the app build files. Keep the workflow and Gradle config in sync.
- Provision an explicit pinned Gradle version via `gradle/actions/setup-gradle` matching the Android Gradle Plugin. Because no local wrapper may exist initially, support a wrapper-free first build by invoking `gradle --no-daemon ...` on the GitHub runner. If a wrapper exists and is valid, either use it consistently or document the reason for the chosen strategy.
- Run the correct project tasks for unit tests, Android lint, and debug APK assembly. Use tasks appropriate to the real Gradle project rather than guessing; typical app tasks are `:app:testDebugUnitTest`, `:app:lintDebug`, and `:app:assembleDebug`.
- Upload `app/build/outputs/apk/debug/*.apk` as a GitHub Actions artifact only after the build succeeds. Give the artifact a clear name, such as `qalb-debug-apk`.
- Keep workflow permissions minimal. Do not use secrets, sign a release APK, publish a release, or automatically publish to the Play Store.
- Use caching where supported by the selected Gradle action. Set reasonable timeout limits.
- Ensure the workflow can be inspected and committed without local Android tooling. Validate YAML with an existing validator if one exists; otherwise manually inspect indentation and expression syntax.

Update README with beginner-friendly instructions for pushing changes, checking the Actions tab, opening failed logs, downloading the artifact, and installing the debug APK on a real Android phone. Clearly distinguish static inspection from a cloud build that has actually passed.

Do not claim the workflow works until a successful run is observed on GitHub. At the end, list changed files and give the exact git commands I should run to commit and push. Stop after CI setup.
