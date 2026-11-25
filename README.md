# Wichtly

Wichtly is an Android app for the person who organises a Secret Santa. It manages groups, participants and
wishlists, performs the draw and keeps every past draw as an unchangeable record. The app works fully
offline; all data stays on the device.

**Team:** Merve Öztürk, Viktoria Ludchenko, Diana Folz, Melissa Meyer

**Course:** Development of Mobile Applications, DHBW Mannheim, winter semester 2026/27

## Statement of responsibility

The group answers for every line of this project, regardless of whether a team member or a coding assistant
wrote it.

## Features

| Area | What it does |
|---|---|
| Groups | Create, edit and delete groups with name, budget, draw day, draw type (classic or white elephant) and photo |
| Participants | Manage the participants of a group, each with name and photo |
| Wishlists | Maintain one wishlist per participant |
| Draw | Draw from three participants on, up to and including the draw day. Nobody draws themselves; everyone gives and receives exactly once. Drawing again creates a new record |
| History | Every draw is stored with the participant names and wishes as they were at that moment (snapshot) |
| Camera | Group and participant photos with CameraX, behind the camera permission. If the permission is denied, the app keeps working without photos |

## Requirements

- Android Studio (current stable release)
- Android SDK Platform 37 (installed through the Android Studio SDK Manager)
- A device or emulator with Android 8.0 (API 26) or newer
- A JDK (17 or newer) to start the Gradle wrapper. Android Studio ships one; on the command line, `JAVA_HOME`
  must point to a JDK. The JDK 25 used by the Gradle daemon is downloaded automatically on the first build
  (`gradle/gradle-daemon-jvm.properties`)

## Build and run

From a clean checkout:

```bash
git clone <repository-url>
cd <repository-folder>
./gradlew :app:assembleDebug        # build the debug APK (Windows: gradlew.bat)
./gradlew :app:installDebug         # install it on a connected device or running emulator
```

Alternatively, open the project folder in Android Studio, wait for the Gradle sync and run the `app`
configuration. Android Studio creates `local.properties` with the SDK path; outside Android Studio, set
`ANDROID_HOME` or create `local.properties` with `sdk.dir=<path to the Android SDK>`.

## Tests and checks

```bash
./gradlew :app:testDebugUnitTest    # unit tests on the JVM, no device needed
./gradlew :app:lintDebug            # Android Lint
```

The unit tests cover the draw algorithm, the business rules and validators, all use cases, all ViewModels,
the error mappers, the routes and the logging facade. Repositories are replaced by in-memory fakes
(`app/src/test/.../testutil`); time and randomness are injected so the tests are deterministic.

## Architecture

One Gradle module, Kotlin, Jetpack Compose with Material 3, MVVM and Clean Architecture.

```
ui (Compose screens, ViewModels)  ──►  domain (models, rules, use cases)  ◄──  data (Room, files)
```

- **domain** has no dependency on Android UI or Room. It contains the models, the business rules
  (`DrawRules`, validators), the draw algorithm and the use cases.
- **Use cases are the only place for validation.** Every write goes through a use case
  (`SaveGiftGroupUseCase`, `SaveParticipantUseCase`, `SaveWishlistItemUseCase`, `PerformDrawUseCase`,
  `DeleteGiftGroupUseCase`, `DeleteParticipantUseCase`). They return a result type with either success or a
  list of business errors. The only exception is deleting a single wish, which has no rules and no file.
- **Repositories are pure persistence.** `observe…` methods return a `Flow` for screens that show live data;
  `get…` methods read the current state once (use cases, loading a form).
- **ViewModels** own the screen state as a `StateFlow` (`combine` + `stateIn(WhileSubscribed(5_000))`) and
  translate business errors into string resources. One-off events such as "saved" are part of the UI state
  and are acknowledged by the route after navigating.
- **Dependency injection** is done by hand: `di/AppContainer` creates repositories and use cases, each
  ViewModel has a `factory(container, …)` and the routes obtain the container through `appContainer()`.

### Package rules

- A package contains either files or sub-packages, never both.
- Every screen has `screen/` (stateless screen, dialogs, UI state, UI models, actions) and `state/`
  (route, ViewModel, error mapping). `screen/` never imports from `state/`.
- Shared building blocks live in `ui/components/{layout,controls,display,photo}`, helpers for ViewModels
  and routes in `ui/common`, colours, typography and spacing in `ui/theme`.
- Tests mirror the package structure of `main`.

## Design decisions

- **Snapshot history:** A draw stores the participant names, the assignments, the receivers' wishes and the
  draw type as a copy. Later changes to participants or wishlists do not alter past results. Deleting a
  group deletes its draws as well.
- **Atomic draw:** `PerformDrawUseCase` reads group, participants and wishes in one Room transaction
  (`DrawDao.getDrawSource`) and stores draw, assignments and wish snapshots in a second one
  (`DrawDao.insertCompleteDraw`).
- **Draw day:** Drawing is allowed up to and including the draw day. Groups stay editable afterwards; a
  changed date must lie in the future.
- **Limits:** Names up to 50 characters, wishes up to 200 characters, budget below €999,999, draw day less
  than 100 years ahead. Group names, participant names within a group and wishes of a participant must be
  unique (case-insensitive).
- **Database:** Room with exported schema (`app/schemas`). From version 4 on, schema changes are added as
  `AutoMigration`; versions 1 to 3 were development versions and are discarded.
- **Photos** are stored in private app storage (`filesDir/photos`); the database keeps relative paths only.
  Paths outside this folder are rejected. Photos without a reference are removed on app start after
  24 hours.
- **Process death:** Form and editor input is kept in the `SavedStateHandle` and survives Android stopping
  the app in the background.
- **Privacy:** Backups and device-to-device transfer are disabled (`allowBackup="false"`,
  `data_extraction_rules.xml`). Logging goes through `AppLog` (levels DEBUG, INFO, WARN, ERROR; tag prefix
  `Wichtly/`; release builds log from INFO on). Names, wishes and assignments are never logged.
- **Coroutines:** Inside coroutines, `CancellationException` is caught first and rethrown; in `Flow.catch`
  via `if (e is CancellationException) throw e`.

## Working with coding assistants

- `AI_USAGE.md` is the log book: setup, every prompt and what was done with the result.
- `app/AGENTS.md` is the instruction file given to the assistants (architecture rules and conventions).
- `.editorconfig` holds the shared formatting rules for Android Studio.
