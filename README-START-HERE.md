# Qalb — OpenCode Starter Kit (GitHub Actions build only)

This starter kit is for building **Qalb**, an offline-first Android adhkar and tasbih app, using OpenCode locally while **all Android compile/test/lint/APK build work runs on GitHub Actions**.

## Your computer does NOT need

- Android Studio
- Android SDK or emulator
- JDK
- Gradle installation

You only need OpenCode, Git, a web browser, and an internet connection. OpenCode edits the source files on your computer; Git pushes them to GitHub; GitHub Actions builds the Android APK on a GitHub-hosted runner.

> Important: GitHub Actions can compile and package the app, but it cannot prove that the UI feels good on your specific phone. Once an APK is available, download it from the Actions artifact and install it on your Android phone for manual checks.

## 1. Keep the project off C: if possible

Create a working folder on a drive with enough free space, for example `E:\forge\qalb` if you run OpenCode natively on Windows. If OpenCode runs inside WSL, use a path visible to that same WSL environment and keep Git/OpenCode commands in one consistent environment. Do not create one repo in Windows and another in WSL.

Extract this kit. Copy `AGENTS.md`, `README-START-HERE.md`, `docs/`, and `prompts/` into the Qalb project root. You can also extract the kit directly into the new `qalb` folder.

## 2. Create an empty GitHub repository

1. On GitHub, create a repository named `qalb` (or choose an available name).
2. For easiest first push, create it **without** README, `.gitignore`, or license files.
3. Choose public only if you are comfortable publishing the source code publicly; otherwise keep it private.
4. Do not add secrets or API keys. The MVP should need none.

## 3. Start Git locally

Open a terminal in the Qalb project root. Check Git is available:

```bash
git --version
```

Initialize and commit the starter documents:

```bash
git init
git branch -M main
git add .
git diff --cached --check
git commit -m "docs: initialize Qalb project plan"
```

Add your real GitHub URL (replace `YOUR_USERNAME`):

```bash
git remote add origin https://github.com/YOUR_USERNAME/qalb.git
git push -u origin main
```

If Git says `origin` already exists, do not add it again; inspect it with `git remote -v`.

## 4. Open the project in OpenCode

From the same project root, launch:

```bash
opencode
```

Use the **Plan** agent first. In the OpenCode TUI, switch agents with `Tab` in the default keymap; if that does not work, use the agent switcher shown by your installed version. The Plan agent is for review/planning; Build is for source edits.

## 5. Run prompts in this exact order

**Do not paste all prompts together.** Open each file, copy its complete contents into OpenCode, and submit it as a separate task.

1. `prompts/01-plan-only.md` — Plan mode. Review scope and architecture only.
2. `prompts/02-create-project.md` — Build mode. OpenCode creates a native Kotlin/Compose project from scratch and adds no requirement for Android Studio.
3. `prompts/03-github-actions-ci.md` — Build mode. Create the cloud build workflow immediately, before implementing all app features.
4. Push the changes and verify the first green GitHub Actions run. Download the debug APK artifact and install it on a phone if possible.
5. `prompts/04-azkar-data-and-library.md` — Build mode.
6. `prompts/05-scheduling-engine.md` — Build mode.
7. `prompts/06-tasbih.md` — Build mode.
8. `prompts/07-settings-backup.md` — Build mode.
9. `prompts/08-review-release.md` — Plan audit first, then Build only for approved fixes.
10. `prompts/09-fix-ci-failure.md` — Use only when Actions fails; paste the real log into OpenCode along with this prompt.

After each prompt that changes files, inspect `git status` and `git diff`, commit the work, then push it. (The planning-only prompt may produce no file changes.) Each push after CI is configured should trigger GitHub Actions. Do not move to the next feature if the current CI run fails. If a run fails, open `prompts/09-fix-ci-failure.md`, paste in the real error log, and use that prompt in Build mode.

## 6. The only build verification loop you need

For each feature:

1. Let OpenCode edit the source files and add tests.
2. Ask OpenCode to run only checks that do not need Android SDK/JDK/Gradle. It must not claim the Android project builds locally.
3. Review the diff and commit/push:

   ```bash
   git status
   git diff --check
   git diff
   git add .
   git commit -m "feat: implement <short-feature-name>"
   git push
   ```

4. Open the GitHub repository → **Actions** → latest run. Open the failed step if the workflow is red. Copy the error and the last useful part of the log into OpenCode with this instruction:

   > This is the real GitHub Actions log from Qalb. Diagnose the root cause first. Do not remove or weaken tests to make CI green. Propose the smallest safe fix, then implement it. Do not claim success until the next GitHub Actions run passes.

5. Commit and push the fix. Re-check the new run.

If the project is private, check the GitHub Actions usage limits and billing settings for your account. Avoid unnecessary workflows and repeated pushes while a run is still active.

## 7. Download and install the APK

When the workflow succeeds:

1. Open the successful Actions run.
2. Scroll to **Artifacts** and download the debug APK artifact.
3. Extract the ZIP if GitHub downloaded an artifact ZIP.
4. Move the APK to your Android phone and install it. You may need to allow installation from that file manager/browser temporarily.
5. Test Arabic/Bengali text, navigation, data persistence, and the feature that was just added.

This is a debug APK, not a Play Store release. Do not commit signing keys or enable automatic publishing as part of the MVP.

## 8. Important rules for OpenCode

- Never ask OpenCode to install Android Studio, Android SDK, JDK, or Gradle on your computer.
- Never use local `gradle`, `./gradlew`, `sdkmanager`, or Android emulator commands as a pass/fail check. All Android build/test/lint checks must run on GitHub Actions.
- The initial repository may not have a Gradle Wrapper. The cloud workflow should provision an explicit, compatible Gradle version using `gradle/actions/setup-gradle` and run Gradle on the GitHub runner. Keep that version aligned with the Android Gradle Plugin version in the project. This avoids requiring local Gradle or generating a binary wrapper locally.
- Use stable, mutually compatible Kotlin, Android Gradle Plugin, Gradle, JDK, and Android SDK versions. Record the chosen versions in the README/workflow; do not guess compatibility.
- Never claim a cloud build passed unless a successful GitHub Actions run proves it.
- Do not invent Quranic text, hadith wording, source references, repetition counts, authenticity grades, or timing rules.
- Keep curated source-verified data separate from personal/user-entered content.
- Never add passwords, personal access tokens, API keys, keystores, or signing secrets to Git.
- Use small tasks, review `git diff`, and commit green milestones.

## MVP completion checklist

- [ ] GitHub Actions builds/tests/lints the app and uploads a debug APK.
- [ ] APK installs and launches on a real Android phone.
- [ ] Arabic RTL and Bengali text render correctly.
- [ ] Users can add/edit/delete/reorder personal adhkar.
- [ ] Curated adhkar show references and review status.
- [ ] Time-window adhkar are distinct from prayer-triggered adhkar.
- [ ] Completing one occurrence does not complete another occurrence by mistake.
- [ ] Tasbih count persists and supports undo/reset.
- [ ] Export/import validates data and does not silently overwrite it.
- [ ] Tests and lint pass in GitHub Actions.
- [ ] No secrets or signing keys are committed.
