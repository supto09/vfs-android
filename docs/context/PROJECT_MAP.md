# Project Map (VFS-GM)

Purpose
- Android app to manage VFS Global visa appointment slot checking with automated authentication.

Entry Points
- `app/src/main/java/com/example/vfsgm/MainActivity.kt`: Activity entry; hosts Compose UI.
- `app/src/main/java/com/example/vfsgm/MyApplication.kt`: app-level init, services setup.
- `app/src/main/java/com/example/vfsgm/ui/screens/AppScreen.kt`: main Compose screen, wires UI actions to ViewModel.

Architecture (MVVM + Repository)
- UI: Compose screens/components collect StateFlow via `collectAsState()`.
- ViewModel: `MainViewModel` orchestrates all flows and background jobs.
- Repositories: Data/Session/AppConfig/Entry store state + persistence.
- APIs: Auth/LeasedAccount/Calendar/Applicant/Slot clients with network calls.
- Network: OkHttp client with persistent cookie jar.

Core Flows
- Auth flow: lease account -> solve Turnstile -> login -> persist session.
- Periodic jobs: re-login loop (about 25 min) and slot-check loop (3+ min).
- Logging: Firebase log/data services for status and slot results.
- Persistence: DataStore for config/session; cookies on disk (see `MyCookieJar`).

State & Data
- State is exposed via StateFlow in repositories; UI observes immutable snapshots.
- API calls return `SealedResult<T>` for explicit success/error handling.

Important Jobs (MainViewModel)
- `reLoginJob`: periodic logout + re-login + post-login actions.
- `checkSlotJob`: periodic slot availability check.
- `loadSlotSlob`: slot loading job lifecycle (note naming).

Key Directories
- `app/src/main/java/com/example/vfsgm/ui/`: Compose UI screens and components.
- `app/src/main/java/com/example/vfsgm/viewmodel/`: Base and main ViewModel.
- `app/src/main/java/com/example/vfsgm/data/`: repositories, APIs, DTOs, constants, network.
- `app/src/main/java/com/example/vfsgm/core/`: services (encryption, Turnstile, Firebase).
- `app/src/main/res/`: resources, themes, icons.

Build & Test (root)
- `./gradlew assembleDebug`
- `./gradlew assembleRelease`
- `./gradlew lint`
- `./gradlew test`

Token-Saving Guidance
- This file is meant as a lightweight reference only.
- Use `FILES_OF_INTEREST.md` to jump directly to relevant files.
- Only open deeper files on-demand for a given task.
