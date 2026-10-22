# Qalb — App Architecture

## Default approach
Use a single Android app module initially. Prefer MVVM, unidirectional UI state, repositories, Room, DataStore, Kotlin Coroutines/Flow, and Jetpack Compose. Avoid premature multi-module architecture.

## Suggested package layout

```text
com.qalb.azkar/
├── data/
│   ├── local/           # Room database, DAO, entities, migrations
│   ├── repository/      # Repository implementations
│   └── seed/            # Versioned curated seed data
├── domain/
│   ├── model/           # Domain models
│   ├── repository/      # Repository interfaces if useful
│   └── usecase/         # Schedule, completion, import/export rules
├── feature/
│   ├── today/
│   ├── library/
│   ├── editor/
│   ├── collections/
│   ├── tasbih/
│   └── settings/
├── navigation/
├── ui/theme/
└── util/
```

Adapt names to the generated project rather than moving every file for cosmetic reasons.

## Responsibilities
- Compose screens render state and emit user actions.
- ViewModels expose state and call domain/repository functions.
- Domain code handles scheduling semantics, completion identity, counting, and import validation.
- Repositories coordinate Room and bundled seed records.
- Room stores editable content, occurrences, collections, completion history, and tasbih state.
- DataStore stores small preferences such as theme, text scale, and vibration preference.

## Important boundaries
- Do not put scheduling decisions inside Composables.
- Do not keep the only copy of a count in a ViewModel or Compose `remember` state.
- Do not use a network call for core functionality.
- Keep source references separate from translated/display text.
- Use versioned Room migrations when the schema evolves; do not destructively recreate a user's database.

## Startup and seed data
Curated starter content should be packaged as versioned JSON or generated database seed records with stable IDs. Validate seed records before use. Never overwrite user edits silently during app upgrades. Document how seed-data version changes are merged into existing local data.

## Time handling
The MVP does not need to calculate astronomical prayer times. Model occasions semantically and display time-window groups chosen by the user/app rules. Keep locale/time-window rules in one testable domain component, and document whether the selected rule follows a reviewed Hanafi/local editorial policy.

## Accessibility and typography
- Use appropriate RTL layout for Arabic content.
- Keep Arabic text selectable and scaleable.
- Provide adequate contrast and touch targets.
- Support system light/dark theme and adjustable Arabic font size.
