# Phase 1 — Planning Only (use Plan mode)

You are the senior Android architect for Qalb. Read `AGENTS.md` and all documents in `docs/` before answering.

Do not edit source code, create the app, install dependencies, or run destructive commands in this phase.

Important environment constraints: I do not have Android Studio; my C: drive is low on free space. I will use OpenCode and Git locally, while **all Android compile/test/lint/APK builds must run on GitHub Actions**. Do not suggest installing Android Studio, JDK, Android SDK, Gradle, or an emulator locally.

Please:
1. Summarise MVP scope and deferred features.
2. Review architecture/data models for missing relationships or overengineering.
3. Propose native Kotlin + Jetpack Compose project scaffolding that can be authored without Android Studio.
4. Design a GitHub Actions baseline workflow that installs/configures Java, Android SDK, and an explicit compatible Gradle version on the GitHub-hosted runner, runs tests/lint/debug APK assemble, and uploads the APK.
5. Ensure the workflow does not depend on a locally generated Gradle Wrapper. If a wrapper is present later, document how CI should use it consistently.
6. Identify required compatibility choices (AGP/Kotlin/Gradle/JDK/compileSdk/build-tools) and tell me how these will be recorded and kept consistent.
7. Identify religious-content risks; never invent source text, references, numbers, or timing rulings.
8. Produce milestones with exact deliverables and exit criteria. CI should be established immediately after the initial scaffold, before feature implementation.
9. Do not ask me to run any local Android build command.

Make conservative assumptions rather than asking many questions. Finish with the single next action I should take. Do not implement code yet.
