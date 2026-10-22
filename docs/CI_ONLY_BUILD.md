# CI-only Android Build Policy

Qalb is edited locally with OpenCode and Git, but Android build tooling is not installed on the developer's computer. All Android compilation, unit tests, Android lint, and APK assembly must run on GitHub Actions.

## Required CI behavior

1. Trigger on each push to `main` and pull requests targeting `main`.
2. Provision a compatible JDK and Android SDK on a GitHub-hosted runner.
3. Install the Android platform/build-tools versions required by the project's `compileSdk` and Gradle configuration.
4. Provision an explicit compatible Gradle version using the official Gradle Actions setup. If the repository has no Gradle Wrapper, use the provisioned `gradle` command on the runner. Keep Gradle and Android Gradle Plugin versions compatible and documented.
5. Run unit tests, Android lint, and debug APK assembly.
6. Upload the debug APK only if those checks succeed.
7. Keep token permissions minimal; no signing secrets or Play Store deployment in the MVP.

## How a developer verifies a change

- Inspect code and `git diff` locally.
- Commit and push the change.
- Open GitHub repository → Actions → open the latest run.
- If red, inspect the first real error and relevant task log. Copy that information into OpenCode; ask it for a root-cause diagnosis and minimal fix.
- Push the fix and confirm a new green run.
- Download the APK artifact and perform manual checks on a real device.

A green unit-test/lint/APK build does not replace physical-device testing. CI does not automatically run the app on the user's phone.
