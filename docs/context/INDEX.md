# Full Index (Load-On-Demand)

Purpose
- Detailed, low-token index of the repo. Open only when required.

High-Level Flow
- UI (Compose) -> MainViewModel -> Repositories -> APIs -> OkHttp + CookieJar.
- Auth loop: lease account -> solve Turnstile -> login -> persist session -> periodic checks.
- Slot flow: check earliest date -> save to Firebase -> load slots -> allocation IDs.

Folders
- `app/src/main/java/com/example/vfsgm/`: app code (UI, ViewModel, data, core).
- `app/src/main/res/`: Android resources (themes, strings, icons, XML rules).
- `app/src/test/` and `app/src/androidTest/`: unit/instrumented tests.
- `gradle/`: Gradle wrapper and versions catalog.
- Root files: build scripts, keystore, agent instructions.

Files (Root)
- `AGENTS.md`: agent instructions and context index policy.
- `CLAUDE.md`: guidance (mirrored into AGENTS.md).
- `build.gradle.kts`: top-level plugins (Android, Kotlin, Firebase).
- `settings.gradle.kts`: repositories + module include.
- `gradle.properties`: Gradle/AndroidX flags.
- `gradle/libs.versions.toml`: dependency and plugin versions.
- `gradlew`, `gradlew.bat`, `gradle/wrapper/*`: Gradle wrapper.
- `debug.keystore`: shared debug signing key.

Files (App Config)
- `app/build.gradle.kts`: Android config, build types, dependencies (OkHttp, Moshi, DataStore, 2Captcha, Firebase).
- `app/proguard-rules.pro`: default ProGuard template.
- `app/google-services.json`: Firebase config (sensitive, not loaded by default).
- `app/src/main/AndroidManifest.xml`: permissions, application, launcher activity.

Files (Entry)
- `app/src/main/java/com/example/vfsgm/MainActivity.kt`: Compose host activity + AppScreen.
- `app/src/main/java/com/example/vfsgm/MyApplication.kt`: initializes CookieJarHolder.

Files (ViewModel)
- `app/src/main/java/com/example/vfsgm/viewmodel/BaseViewModel.kt`: shared repos/APIs + job handles.
- `app/src/main/java/com/example/vfsgm/viewmodel/MainViewModel.kt`: auth, periodic jobs, slot checks, app actions.

Files (Repositories)
- `app/src/main/java/com/example/vfsgm/data/repository/AppConfigRepository.kt`: DataStore-backed app config state.
- `app/src/main/java/com/example/vfsgm/data/repository/SessionRepository.kt`: DataStore-backed session state.
- `app/src/main/java/com/example/vfsgm/data/repository/EntryRepository.kt`: loads static Entry from API.
- `app/src/main/java/com/example/vfsgm/data/repository/DataRepository.kt`: in-memory data/job state.

Files (DataStore)
- `app/src/main/java/com/example/vfsgm/data/store/AppConfigStore.kt`: preferences store for device index.
- `app/src/main/java/com/example/vfsgm/data/store/SessionDataStore.kt`: preferences store for access token + username.
- `app/src/main/java/com/example/vfsgm/data/store/TurnstileStore.kt`: in-memory token queue with expiry.

Files (DTO)
- `app/src/main/java/com/example/vfsgm/data/dto/AppConfig.kt`: device index.
- `app/src/main/java/com/example/vfsgm/data/dto/SessionData.kt`: access token + username.
- `app/src/main/java/com/example/vfsgm/data/dto/DataState.kt`: urn, dates, allocation IDs, job state.
- `app/src/main/java/com/example/vfsgm/data/dto/Entry.kt`: entry + applicants.
- `app/src/main/java/com/example/vfsgm/data/dto/LeasedAccount.kt`: leased account credentials.

Files (Constants)
- `app/src/main/java/com/example/vfsgm/data/constants/Global.kt`: date format + hardcoded 2Captcha/Site keys (sensitive).
- `app/src/main/java/com/example/vfsgm/data/constants/CountryCode.kt`: country enum (PAK).
- `app/src/main/java/com/example/vfsgm/data/constants/MissionCode.kt`: mission enum (UKR).
- `app/src/main/java/com/example/vfsgm/data/constants/VisaApplicationCenterCode.kt`: VAC enum (LHE).
- `app/src/main/java/com/example/vfsgm/data/constants/VisaCategoryCode.kt`: category enum (IP).
- `app/src/main/java/com/example/vfsgm/data/constants/Gender.kt`: gender enum with id mapping.
- `app/src/main/java/com/example/vfsgm/data/constants/Nationality.kt`: nationality enum with ids and ISO.
- `app/src/main/java/com/example/vfsgm/data/constants/Rough.kt`: commented JSON notes.

Files (APIs)
- `app/src/main/java/com/example/vfsgm/data/api/AuthApi.kt`: login with RSA/OAEP + Cloudflare token.
- `app/src/main/java/com/example/vfsgm/data/api/LeasedAccountApi.kt`: lease account, report block, static entry.
- `app/src/main/java/com/example/vfsgm/data/api/ApplicantApi.kt`: add/load applicants (OkHttp, JSON body).
- `app/src/main/java/com/example/vfsgm/data/api/CalenderApi.kt`: check slot availability + load calendar.
- `app/src/main/java/com/example/vfsgm/data/api/SlotApi.kt`: load timeslots + allocation IDs.

Files (Network)
- `app/src/main/java/com/example/vfsgm/data/network/NewOkHttpClient.kt`: OkHttp config + interceptors + cookie jar.
- `app/src/main/java/com/example/vfsgm/data/network/MyCookieJar.kt`: persistent cookie jar with base-domain bucketing.
- `app/src/main/java/com/example/vfsgm/data/network/OkHttpExtensions.kt`: `Call.await()` coroutine helper.
- `app/src/main/java/com/example/vfsgm/data/network/PublicIpManager.kt`: fetch and cache public IP.
- `app/src/main/java/com/example/vfsgm/data/network/MyAgentHolder.kt`: mutable User-Agent holder.

Files (Core)
- `app/src/main/java/com/example/vfsgm/core/EncryptionManager.kt`: RSA/OAEP/SHA-256 encryption.
- `app/src/main/java/com/example/vfsgm/core/ClientSourceManager.kt`: encrypted clientsource header.
- `app/src/main/java/com/example/vfsgm/core/SealedResult.kt`: success/error wrapper.
- `app/src/main/java/com/example/vfsgm/core/JitterService.kt`: random delay for polling.
- `app/src/main/java/com/example/vfsgm/core/TurnstileService.kt`: 2Captcha Turnstile solver.
- `app/src/main/java/com/example/vfsgm/core/CfCookieCheckManager.kt`: wait for `cf_clearance` cookie.
- `app/src/main/java/com/example/vfsgm/core/FirebaseLogService.kt`: log to `z_logs/{deviceIndex}`.
- `app/src/main/java/com/example/vfsgm/core/FirebaseDataService.kt`: read/write earliest date.

Files (UI Screens)
- `app/src/main/java/com/example/vfsgm/ui/screens/AppScreen.kt`: main UI; routes actions to ViewModel.

Files (UI Components)
- `app/src/main/java/com/example/vfsgm/ui/components/CloudflareBypassWebviewModal.kt`: modal wrapper for CF WebView.
- `app/src/main/java/com/example/vfsgm/ui/components/CloudflareBypassWebview.kt`: WebView CF bypass + cookie sync.
- `app/src/main/java/com/example/vfsgm/ui/components/TurnstileWebviewModal.kt`: modal for Turnstile WebView.
- `app/src/main/java/com/example/vfsgm/ui/components/TurnstileTokenWebview.kt`: embedded Turnstile widget.
- `app/src/main/java/com/example/vfsgm/ui/components/SettingsBottomSheet.kt`: device index editor.

Files (UI Atomics)
- `app/src/main/java/com/example/vfsgm/ui/components/atomics/MySolidButton.kt`: solid button variants.
- `app/src/main/java/com/example/vfsgm/ui/components/atomics/MyOutlinedButton.kt`: outlined button variants.

Files (UI Organisms)
- `app/src/main/java/com/example/vfsgm/ui/components/organism/SystemControlPanel.kt`: start/stop relogin + modals.
- `app/src/main/java/com/example/vfsgm/ui/components/organism/AuthControlPanel.kt`: login control.
- `app/src/main/java/com/example/vfsgm/ui/components/organism/AppControlPanel.kt`: slot and app actions.

Files (UI Theme)
- `app/src/main/java/com/example/vfsgm/ui/theme/Theme.kt`: Material3 theme + dynamic colors.
- `app/src/main/java/com/example/vfsgm/ui/theme/Color.kt`: color constants.
- `app/src/main/java/com/example/vfsgm/ui/theme/Type.kt`: typography.

Files (Resources)
- `app/src/main/res/values/strings.xml`: app name.
- `app/src/main/res/values/themes.xml`: app theme.
- `app/src/main/res/values/colors.xml`: color resources.
- `app/src/main/res/xml/backup_rules.xml`: backup rules template.
- `app/src/main/res/xml/data_extraction_rules.xml`: data extraction rules template.
- `app/src/main/res/drawable/*`: launcher background/foreground.
- `app/src/main/res/mipmap-*/*`: launcher icons.

Files (Tests)
- `app/src/test/java/com/example/vfsgm/ExampleUnitTest.kt`: default unit test.
- `app/src/androidTest/java/com/example/vfsgm/ExampleInstrumentedTest.kt`: default instrumented test.

Token-Saving Policy
- Do not open this file by default.
- Open only the sections needed for a task.
