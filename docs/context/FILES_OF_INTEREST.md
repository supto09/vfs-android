# Files Of Interest

App Entry + UI
- `app/src/main/java/com/example/vfsgm/MainActivity.kt`: Activity entry.
- `app/src/main/java/com/example/vfsgm/MyApplication.kt`: Application class.
- `app/src/main/java/com/example/vfsgm/ui/screens/AppScreen.kt`: main Compose screen.
- `app/src/main/java/com/example/vfsgm/ui/components/organism/SystemControlPanel.kt`: system actions UI.
- `app/src/main/java/com/example/vfsgm/ui/components/organism/AuthControlPanel.kt`: auth UI.
- `app/src/main/java/com/example/vfsgm/ui/components/organism/AppControlPanel.kt`: app actions UI.

ViewModels
- `app/src/main/java/com/example/vfsgm/viewmodel/BaseViewModel.kt`: shared repos/APIs.
- `app/src/main/java/com/example/vfsgm/viewmodel/MainViewModel.kt`: primary logic + jobs.

Repositories + Store
- `app/src/main/java/com/example/vfsgm/data/repository/DataRepository.kt`: in-memory data state.
- `app/src/main/java/com/example/vfsgm/data/repository/SessionRepository.kt`: session state, DataStore.
- `app/src/main/java/com/example/vfsgm/data/repository/AppConfigRepository.kt`: config state, DataStore.
- `app/src/main/java/com/example/vfsgm/data/repository/EntryRepository.kt`: entry state.
- `app/src/main/java/com/example/vfsgm/data/store/SessionDataStore.kt`: session persistence.
- `app/src/main/java/com/example/vfsgm/data/store/AppConfigStore.kt`: config persistence.
- `app/src/main/java/com/example/vfsgm/data/store/TurnstileStore.kt`: Turnstile token persistence.

APIs
- `app/src/main/java/com/example/vfsgm/data/api/AuthApi.kt`: login/auth calls.
- `app/src/main/java/com/example/vfsgm/data/api/LeasedAccountApi.kt`: lease account / entry actions.
- `app/src/main/java/com/example/vfsgm/data/api/CalenderApi.kt`: calendar endpoints.
- `app/src/main/java/com/example/vfsgm/data/api/SlotApi.kt`: slot queries.
- `app/src/main/java/com/example/vfsgm/data/api/ApplicantApi.kt`: applicant CRUD.

Network
- `app/src/main/java/com/example/vfsgm/data/network/NewOkHttpClient.kt`: OkHttp builder.
- `app/src/main/java/com/example/vfsgm/data/network/MyCookieJar.kt`: persistent cookies.
- `app/src/main/java/com/example/vfsgm/data/network/OkHttpExtensions.kt`: helpers/interceptors.
- `app/src/main/java/com/example/vfsgm/data/network/PublicIpManager.kt`: IP fetch.

Core Services
- `app/src/main/java/com/example/vfsgm/core/TurnstileService.kt`: Cloudflare token solving.
- `app/src/main/java/com/example/vfsgm/core/EncryptionManager.kt`: RSA/OAEP login encryption.
- `app/src/main/java/com/example/vfsgm/core/FirebaseLogService.kt`: log sink.
- `app/src/main/java/com/example/vfsgm/core/FirebaseDataService.kt`: data sink.
- `app/src/main/java/com/example/vfsgm/core/CfCookieCheckManager.kt`: cookie readiness.
- `app/src/main/java/com/example/vfsgm/core/SealedResult.kt`: result wrapper.

Config + Build
- `app/build.gradle.kts`
- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle/libs.versions.toml`
- `app/src/main/AndroidManifest.xml`

Token-Saving Guidance
- Use this list to open only the files you need.
- Do not load whole directories unless the task demands it.
