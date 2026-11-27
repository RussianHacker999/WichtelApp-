# Wichtly

Wichtly is an offline Android app for organising a Secret Santa. It manages groups, participants and
wishlists, performs the draw and keeps every past draw as an unchangeable record. All data stays on the
device.

| | |
|---|---|
| **Team** | Merve Öztürk, Viktoria Ludchenko, Diana Folz, Melissa Meyer |
| **Course** | Development of Mobile Applications, DHBW Mannheim, winter semester 2026/27 |
| **Platform** | Android 8.0 (API 26) and newer |
| **Stack** | Kotlin, Jetpack Compose (Material 3), Room, CameraX, Navigation Compose |

## Statement of responsibility

The group answers for every line of this project, regardless of whether a team member or a coding assistant
wrote it.

## Features

| Area | Description |
|---|---|
| Groups | Create, edit and delete groups with name, budget, draw day, draw type (classic or white elephant) and photo |
| Participants | Manage the participants of a group, each with a name and a photo |
| Wishlists | Maintain one wishlist per participant |
| Draw | Draw with at least three participants, up to and including the draw day. Nobody draws themselves; everyone gives and receives exactly once |
| History | Every draw is stored as a snapshot of the names and wishes at that moment; drawing again creates a new record |
| Camera | Group and participant photos via CameraX. If the camera permission is denied, the app keeps working without photos |

## Getting started

### Requirements

- Android Studio (current stable release) with Android SDK Platform 37
- JDK 17 or newer to start the Gradle wrapper (Android Studio ships one). The JDK 25 toolchain for the
  Gradle daemon is downloaded automatically on the first build
- A device or emulator with Android 8.0 (API 26) or newer

### Build and run

```bash
git clone <repository-url>
cd <repository-folder>
./gradlew :app:assembleDebug        # build the debug APK (Windows: gradlew.bat)
./gradlew :app:installDebug         # install it on a connected device or a running emulator
```

Alternatively, open the project in Android Studio, wait for the Gradle sync and run the `app` configuration.
Outside Android Studio, set `ANDROID_HOME` or create `local.properties` with `sdk.dir=<path to the Android SDK>`.

### Tests and checks

```bash
./gradlew :app:testDebugUnitTest    # JVM unit tests, no device needed
./gradlew :app:lintDebug            # Android Lint (report in app/build/reports/)
./gradlew ktlintCheck               # Kotlin code style (ktlint)
./gradlew ktlintFormat              # fix style violations automatically
```

The code style is ktlint with the Android Studio style, a line limit of 140 and trailing commas; the rules are
in [`.editorconfig`](.editorconfig), which Android Studio's formatter reads as well.

The unit tests cover the draw algorithm, business rules and validators, all use cases and ViewModels, the
error mappers, the navigation routes and the logging facade. Repositories are replaced by in-memory fakes,
and time and randomness are injected, so the tests are deterministic.

## Architecture

Single-module app following MVVM and Clean Architecture. The domain layer has no dependency on Android UI
or Room; the UI and data layers depend on it.

```
ui (Compose screens, ViewModels)  ──►  domain (models, rules, use cases)  ◄──  data (Room, photo files)
```

```
app/src/main/java/com/example/wichtly/
├── data/        Room database, DAOs, entities, mappers, photo storage, repository implementations
├── di/          AppContainer (manual dependency injection)
├── domain/      models, business rules, validation, use cases, draw algorithm
├── logging/     AppLog facade
├── navigation/  routes and the navigation graph
└── ui/          screens and ViewModels per feature, shared components, theme
```

Every write goes through a use case, which is also the only place where input is validated. ViewModels
read data through repositories and expose the screen state as a `StateFlow`. The detailed rules for
contributors are in [`app/AGENTS.md`](app/AGENTS.md).

## Design decisions

- **Snapshot history:** A draw copies participant names, assignments, wishes and the draw type. Later
  changes to participants or wishlists never alter past results.
- **Atomic draw:** Reading the input and saving the result each run in a single Room transaction.
- **Draw day:** Drawing is possible up to and including the draw day. Groups stay editable afterwards; a
  changed date must lie in the future.
- **Database:** The Room schema is exported to `app/schemas`. From version 4 on, schema changes are
  applied as `AutoMigration`, so user data is kept.
- **Photos:** Stored in private app storage; the database keeps relative paths only. Unreferenced photos
  are removed at app start.
- **Privacy:** Backup and device-to-device transfer are disabled. Logs never contain names, wishes or
  assignments.

## Development environment

The versions the app was actually developed, built and tested with:

| Component | Version |
|---|---|
| Operating system | Windows 11 (x64) |
| Android Studio | 2026.2.2 (build AI-262.10968.63.2622.16730706) |
| JDK | JetBrains Runtime 25.0.4 (bundled with Android Studio, also used as `JAVA_HOME`) |
| Gradle (wrapper) | 9.6.0 |
| Gradle daemon toolchain | Java 25 (`gradle/gradle-daemon-jvm.properties`) |
| Android Gradle Plugin | 9.5.0 |
| Kotlin | 2.4.21 |
| KSP | 2.3.12 |
| Java source/target compatibility | 11 |
| compileSdk / targetSdk | 37 |
| minSdk | 26 (Android 8.0) |
| Android SDK Build-Tools | 36.0.0 |
| Android SDK Platform-Tools (adb) | 37.0.1 |

Library versions are defined in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

### Test devices

| Device | Model | Android version (API level) | Connection |
|---|---|---|---|
| Xiaomi Redmi 14C | 2409BRN2CY | Android 16 (API 36), Xiaomi HyperOS 3.0 | Wireless debugging (`adb` over Wi-Fi) |

## Use of coding assistants

- [`AI_USAGE.md`](AI_USAGE.md) is the log book: tools, every prompt and what was done with the result.
- [`app/AGENTS.md`](app/AGENTS.md) holds the shared instructions for all coding assistants (for example
  Gemini in Android Studio).
- [`CLAUDE.md`](CLAUDE.md) adds the Claude Code specifics and imports `app/AGENTS.md`.
