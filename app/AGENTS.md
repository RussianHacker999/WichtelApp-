# AGENTS.md

Shared instructions for every coding assistant working on Wichtly (Gemini in Android Studio, Claude Code,
GitHub Copilot and others). These rules describe the current state of the code base; new code must follow
them, and this file is updated whenever a rule changes.

## 1. Project

Wichtly is an offline-only Android app for the person who organises a Secret Santa.

- Groups: name, budget, draw day, draw type (`NORMAL` or `JUNK`) and an optional photo
- Participants: name and optional photo; one wishlist per participant
- Draw: random assignment, results overview, reveal per giver including the receiver's wishlist
- History: every completed draw is stored permanently as an immutable snapshot

User-facing texts are German and live in `res/values/strings.xml`. Code, identifiers, comments, log
messages and commit messages are English.

## 2. Tech stack

- Kotlin only, single Gradle module `:app`, minSdk 26, compileSdk/targetSdk 37
- Jetpack Compose with Material 3, Navigation Compose
- Room (with KSP) for persistence, CameraX for photos
- Coroutines and `Flow`; manual dependency injection (no Hilt/Koin)
- Dependencies are declared only in `gradle/libs.versions.toml`

Do not add libraries, change Gradle versions or change the Room schema without an explicit request.

## 3. Architecture

MVVM with Clean Architecture. Dependencies point inwards: `ui → domain ← data`.

| Package | Responsibility |
|---|---|
| `domain/` | Models, business rules (`DrawRules`), validators and error types (`validation/`), use cases, result types (`result/`), repository interfaces, draw algorithm. No Android UI or Room imports |
| `data/` | Room entities, DAOs, relations, mappers, `LocalPhotoStorageManager`, repository implementations |
| `di/` | `AppContainer` creates repositories and use cases |
| `ui/<feature>/screen/` | Stateless screens, dialogs, UI state, UI models, action bundles |
| `ui/<feature>/state/` | Route, ViewModel, error mapping (`*Errors.kt`) |
| `ui/components/`, `ui/theme/`, `ui/common/` | Shared building blocks, design tokens, ViewModel helpers |

Package rules:

- A package contains either files or sub-packages, never both (the root package is the only exception).
- `screen/` never imports from `state/`.
- Tests mirror the package structure of `main`.

## 4. Rules by layer

### Domain and data

- **Use cases are the only place for validation.** Every write (create, update, delete, draw) goes through a
  use case. ViewModels use repositories for reading only.
- Save use cases and `PerformDrawUseCase` return a sealed `<Entity>Result` (`Success` or `ValidationError`
  with a list of errors); delete use cases return `DeleteResult` (`Deleted` or `NotFound`).
- Validation errors are `data object`s in `domain/validation`. Limits are constants in the validators; no
  other layer repeats the numbers.
- Repositories are pure persistence: `observe…()` returns a `Flow` for live screens, `get…()` is a
  `suspend` one-off read, `update…()` returns `false` if the row no longer exists.
- Photos are stored in private storage via `PhotoRepository`; Room keeps the relative path only.
  ViewModels and use cases delete photos only through `deletePhotoQuietly`.
- A schema change requires a new database version and an `AutoMigration` (see `AppDatabase`).

### ViewModels and state

- Each ViewModel owns its screen state as one `StateFlow<…UiState>`. ViewModels that observe Room build it
  with `combine(...).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), …)`.
- Every loading screen has a loading, an error and a ready state (and a not-found state where an ID is
  loaded). Read errors reach the state through `asResult()`, not through a separate flag.
- One-off events (saved, deleted, draw completed) are fields of the UI state; the route reacts in a
  `LaunchedEffect` and acknowledges them with `on…Handled()`.
- Form and editor input that must survive process death is kept in the `SavedStateHandle`.
- Each ViewModel provides `companion object { fun factory(container: AppContainer, …) }`; routes obtain the
  container with `appContainer()`.
- Business errors are translated into string resources in `state/*Errors.kt`; write failures use
  `saveErrorMessage()`.

### Compose UI

- Screens are stateless: state in, callbacks (or an `…Actions` bundle) out. Only routes know ViewModels.
- The `modifier` parameter is applied to the top-level element only.
- Every composable has at least one `@Preview` wrapped in `WichtlyTheme`.
- Screens (except the full-screen start screen) use `WichtlyScaffold` and `PageHeader`; cards, buttons and
  dialogs come from `ui/components`.
- Spacing comes from `Spacing`, colours from `ui/theme/Color.kt`, shapes and typography from the theme.
  No hard-coded strings outside previews.
- User-triggered navigation is wrapped in `dropUnlessResumed` / `dropUnlessResumedWithArg`.

### Coroutines, errors and logging

- Inside coroutines, catch `CancellationException` first and rethrow it; in `Flow.catch` use
  `if (e is CancellationException) throw e`.
- Log only through `AppLog` with `private const val TAG = "<ClassName>"`. Levels: DEBUG for flow and
  rejected input, INFO for successful state changes, WARN for handled problems, ERROR for unexpected
  exceptions that fail a user action.
- Never log names, wish texts, draw assignments or image content. IDs, counts and error types are fine.

### Comments

- KDoc on every public type and every function whose behaviour is not obvious: a one-sentence summary,
  then the contract (meaning of `null`, side effects, errors). Use `@property` for data classes and
  `@param`/`@return` only when they add information.
- Comments explain why, not what. No commented-out code, no change history, no banner comments.

## 5. Feature specifications

### 5.1 Draw

The organiser can perform a random assignment, view all results, select a giver and reveal whom they give
a present to, including the receiver's wishlist.

Invariants for every draw (`SecretSantaDrawAlgorithm`):

- Nobody draws themselves.
- Every participant gives a present to exactly one person and receives one from exactly one person.
- At least `DrawRules.MIN_PARTICIPANTS` (3) participants; drawing is allowed up to and including the draw day.
- `NORMAL` and `JUNK` follow the same rules; the draw type only changes the meaning for the users.

Drawing again with the same participants creates a new record and never overwrites earlier draws.

### 5.2 History and snapshots

- All completed draws are stored permanently and can be listed, opened and inspected.
- Participant names, assignments, the receivers' wishes and the draw type are copied at draw time. Later
  changes to participants or wishlists must not alter historical results.
- Current data and historical draw data stay separate (no foreign keys from snapshots to participants or
  wishes). Deleting a group deletes its history as well.

### 5.3 Camera (CameraX)

- Photos for groups and participants are captured with CameraX and saved to private internal storage;
  Room stores only the relative path.
- `Manifest.permission.CAMERA` is requested at runtime with an explanation dialog first. If it is denied,
  the app keeps working without photos and offers a link to the system settings.

## 6. Quality gate

Before a change is considered done:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

- New logic in use cases, validators, mappers and ViewModels gets JVM unit tests. Use the fakes in
  `app/src/test/.../testutil`; inject time (`today`, `now`) and randomness instead of using the real clock.
- Lint must report no errors.
- Commit messages follow `type(scope): summary` (`feat`, `fix`, `refactor`, `docs`, `test`, `chore`).
- Every prompt and what was done with the result is recorded in `AI_USAGE.md`.
