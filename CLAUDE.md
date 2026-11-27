# CLAUDE.md

Instructions for Claude Code in this repository. The architecture, coding rules and feature specifications
shared by all coding assistants are in `app/AGENTS.md` and apply here in full:

@app/AGENTS.md

## Commands

```bash
./gradlew :app:testDebugUnitTest                      # JVM unit tests
./gradlew :app:assembleDebug                          # debug build
./gradlew :app:lintDebug                              # Android Lint, report in app/build/reports/
./gradlew ktlintFormat ktlintCheck                    # format, then verify the Kotlin code style
./gradlew :app:testDebugUnitTest --tests "*PerformDrawUseCaseTest"   # single test class
```

On Windows use `gradlew.bat` or the Git Bash shell. `assembleRelease` needs network access for R8 mappings.

## Working in this repository

- Read the files you change and their tests first; follow the existing pattern of the nearest feature
  (for example `ui/wishlist` for a list screen with an editor dialog).
- Keep changes scoped to the request. Do not refactor working code, rename public APIs or change the Room
  schema unless asked; a schema change needs a version bump and an `AutoMigration`.
- After every change run the quality gate from `app/AGENTS.md` section 6 and report the result honestly,
  including failing tests.
- Case-only file renames on Windows need `git mv` followed by `./gradlew clean`.
- Do not commit or push unless asked. Commit messages use `type(scope): summary`.

## Documentation duties

- Rules that change in the code are changed in `app/AGENTS.md` in the same change.
- Prompts and outcomes are recorded for `AI_USAGE.md` (date, author, tool, prompt verbatim, accepted /
  changed / rejected, how it was checked, commits). Do not edit `AI_USAGE.md` unless asked.
- Comments, KDoc and log messages are English; user-facing texts stay German in `strings.xml`.
