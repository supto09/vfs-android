# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean assembleDebug

# Run lint checks
./gradlew lint

# Run unit tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.example.vfsgm.ExampleUnitTest"
```

## Architecture Overview

**VFS-GM** is an Android app for managing VFS Global visa appointment slot checking with automated authentication.

### MVVM + Repository Pattern

```
Compose UI (AppScreen.kt)
    ↓ collectAsState()
ViewModel (MainViewModel extends BaseViewModel)
    ↓ viewModelScope coroutines
Repositories (DataRepository, SessionRepository, AppConfigRepository, SubjectRepository)
    ↓
APIs (AuthApi, SubjectApi, CalenderApi, ApplicantApi)
    ↓
OkHttp with Custom CookieJar (MyCookieJar - file-based persistence)
```

### Key Components

- **BaseViewModel**: Base class providing shared repository and API instances - all ViewModels extend this
- **MainViewModel**: Main business logic including periodic job management (reLogin every 25min, slot checking every 3+min)
- **SealedResult<T>**: Sealed interface for explicit success/error handling across API calls
- **MyCookieJar**: File-based persistent cookie storage with domain bucketing at `app_data/cookies/`

### Data Flow

- **State Management**: Kotlin StateFlow/MutableStateFlow with `update()` for immutable state copies
- **Persistence**: DataStore for session/config, file I/O for cookies
- **Network**: OkHttp 4.12 with interceptors for logging and headers

### Authentication Flow

1. Lease visa account via SubjectApi
2. Solve Cloudflare Turnstile CAPTCHA (2Captcha service or WebView fallback)
3. Login with RSA/OAEP/SHA256 encrypted password via AuthApi
4. Store access token in SessionDataStore

### Firebase Integration

- Logs operations to Realtime DB at `z_logs/{deviceIndex}`
- Saves slot dates to `z_earliest_date/{countryCode}/{missionCode}`

## Project Structure

```
app/src/main/java/com/example/vfsgm/
├── viewmodel/          # BaseViewModel, MainViewModel
├── ui/
│   ├── screens/        # AppScreen (main composable)
│   ├── components/     # organism/ (panels), atomics/ (buttons)
│   └── theme/          # Material3 theming
├── data/
│   ├── api/            # REST API clients (AuthApi, SubjectApi, etc.)
│   ├── repository/     # Data abstraction layer
│   ├── dto/            # Data classes
│   ├── store/          # DataStore persistence
│   ├── network/        # OkHttp client, CookieJar
│   └── constants/      # Enums (CountryCode, MissionCode, etc.)
└── core/               # EncryptionManager, TurnstileService, FirebaseLogService
```

## Key Patterns

- Access repositories through BaseViewModel, not directly in UI
- Use `viewModelScope` for all coroutines to respect lifecycle
- Cancel periodic jobs (`reLoginJob`, `checkSlotJob`) on logout/cleanup
- Wrap network calls in try-catch and return `SealedResult`
- Use `collectAsState()` in Compose to observe Flow state