# Migration Plan: Android to Compose Multiplatform (iOS target)

## Context

The app is a single-module Android Habit Tracker using Jetpack Compose, Hilt, Room, Firebase Auth/Firestore, Credential Manager, and Navigation3. The goal is to restructure it as a Compose Multiplatform (CMP) project so the UI and business logic are shared between Android and iOS, while platform-specific APIs (auth, database builder, theming) use `expect/actual`.

---

## Library Replacements

| Current (Android-only) | Replacement (KMP) | Rationale |
|---|---|---|
| Hilt 2.59.2 | **Koin 4.x** | Only mature DI for KMP; has `koin-compose-viewmodel` |
| Room 2.8.4 | **Room KMP 2.8.4** (keep) | Room supports KMP since 2.7.0 -- no rewrite needed |
| Firebase Auth/Firestore (Google SDK) | **GitLive `firebase-kotlin-sdk`** | Production-ready KMP wrapper, coroutine-first API |
| Credential Manager / Google Sign-In | **`expect/actual` AuthService** | Inherently platform-specific |
| Navigation3 | **Manual stack navigation** | App has 4 routes; a library is overkill |
| `java.time.LocalDate` | **`kotlinx-datetime`** | Drop-in KMP replacement |
| `java.util.UUID` | **`kotlin.uuid.Uuid`** | Available in Kotlin 2.0+ stdlib |
| `android.util.Log` | **Kermit** (co.touchlab) | Standard KMP logging |
| Jetpack ViewModel | **Lifecycle ViewModel KMP** | `androidx.lifecycle:lifecycle-viewmodel` supports KMP since 2.8.0 |
| Compose BOM / Material3 | **JetBrains Compose `material3`** | CMP's equivalent of Jetpack Compose |
| Dynamic color (`dynamicDarkColorScheme`) | **`expect/actual` theme** | Android-only; iOS gets static scheme |

---

## Target Project Structure

```
HabitTrackerAndroid2/
  settings.gradle.kts
  build.gradle.kts
  gradle/libs.versions.toml

  composeApp/                          # replaces app/
    build.gradle.kts                   # KMP: android + iosArm64 + iosSimulatorArm64 + iosX64
    src/
      commonMain/kotlin/.../
        domain/model/Habit.kt, HabitType.kt
        data/local/HabitEntity.kt, HabitDao.kt, HabitDatabase.kt, Mappers.kt
        data/remote/FirestoreHabitDataSource.kt   (GitLive Firestore)
        data/repository/HabitRepository.kt, HabitRepositoryImpl.kt
        di/AppModule.kt                           (Koin)
        navigation/AppRoute.kt, Navigator.kt      (simple stack)
        ui/auth/LoginScreen.kt, LoginViewModel.kt
        ui/habitlist/HabitListScreen.kt, HabitListViewModel.kt
        ui/createhabit/CreateHabitScreen.kt, CreateHabitViewModel.kt
        ui/edithabit/EditHabitScreen.kt, EditHabitViewModel.kt
        ui/theme/Color.kt, Type.kt, Theme.kt      (expect)
        platform/AuthService.kt                    (expect)
        platform/DatabaseBuilder.kt                (expect)
        App.kt                                     (root composable)

      androidMain/kotlin/.../
        platform/AuthServiceAndroid.kt             (Credential Manager)
        platform/DatabaseBuilderAndroid.kt         (Room + Context)
        ui/theme/ThemeAndroid.kt                   (dynamic color)
        HabitTrackerApp.kt, MainActivity.kt
      androidMain/res/, AndroidManifest.xml, google-services.json

      iosMain/kotlin/.../
        platform/AuthServiceIos.kt                 (GIDSignIn or Apple Sign-In)
        platform/DatabaseBuilderIos.kt             (Room + NSHomeDirectory)
        ui/theme/ThemeIos.kt                       (static scheme)
        MainViewController.kt                      (ComposeUIViewController)

  iosApp/                              # Xcode project
    iOSApp.swift, ContentView.swift
    GoogleService-Info.plist
```

---

## Migration Phases

### Phase 0: Project Restructuring (Build System)

- Create `composeApp/build.gradle.kts` with KMP plugin (`kotlin.multiplatform`, `org.jetbrains.compose`, `com.android.application`, KSP, google-services)
- Update `settings.gradle.kts`: add JetBrains Compose plugin repo, replace `:app` with `:composeApp`
- Update `libs.versions.toml`: add CMP, Koin, kotlinx-datetime, GitLive Firebase, Kermit; remove Hilt, Nav3, Compose BOM
- Move all existing source from `app/` into `composeApp/src/androidMain/` initially
- Verify Android still builds and runs identically
- Delete old `app/` module

**Critical files:** `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `libs.versions.toml`

### Phase 1: Domain Layer to commonMain

- Move `Habit.kt`, `HabitType.kt`, `HabitRepository.kt` (interface) to `commonMain`
- These are pure Kotlin with no Android deps -- zero changes needed

### Phase 2: Replace JVM-specific APIs

- Add `kotlinx-datetime` to `commonMain`
- In `Mappers.kt`: replace `java.time.LocalDate` with `kotlinx.datetime.LocalDate`
- In `CreateHabitViewModel`: replace `UUID.randomUUID()` with `kotlin.uuid.Uuid.random()`
- Move `Mappers.kt` to `commonMain`

### Phase 3: Room KMP

- Move `HabitEntity.kt`, `HabitDao.kt`, `HabitDatabase.kt` to `commonMain`
- Create `expect fun getDatabaseBuilder(): RoomDatabase.Builder<HabitDatabase>` in `commonMain`
- `androidMain` actual: `Room.databaseBuilder(context, ...)` (existing logic)
- `iosMain` actual: `Room.databaseBuilder(name = path, factory = { HabitDatabase::class.instantiateImpl() })`
- Configure KSP for Room across all targets (kspAndroid, kspIosArm64, kspIosSimulatorArm64, kspIosX64)

### Phase 4: Firebase KMP (GitLive)

- Add `dev.gitlive:firebase-auth` and `dev.gitlive:firebase-firestore` to `commonMain`
- Rewrite `FirestoreHabitDataSource.kt`: replace Google Firestore imports with GitLive equivalents (API is similar, but coroutine-native -- no `.await()`)
- Rewrite `HabitRepositoryImpl.kt`: replace Google FirebaseAuth with GitLive, replace `android.util.Log` with Kermit
- Remove `kotlinx-coroutines-play-services` dependency
- Move both files to `commonMain`

### Phase 5: DI Migration (Hilt to Koin)

- Add Koin dependencies: `koin-core`, `koin-compose`, `koin-compose-viewmodel` (commonMain), `koin-android` (androidMain)
- Create `di/AppModule.kt` in `commonMain` with all Koin module definitions
- Create platform-specific Koin modules for Context (Android) and AuthService impls
- Remove all Hilt annotations (`@HiltViewModel`, `@Inject`, `@AndroidEntryPoint`, `@HiltAndroidApp`, etc.)
- Remove Hilt plugin and dependencies
- Initialize Koin in `HabitTrackerApp.onCreate()` (Android) and from Swift `init()` (iOS)

### Phase 6: Authentication Abstraction

- Define `AuthService` interface in `commonMain` with `suspend fun signInWithGoogle(): AuthResult`
- `androidMain`: implement with Credential Manager (move existing logic from `LoginViewModel`)
- `iosMain`: implement with `GIDSignIn` iOS SDK (via Swift interop) or Apple Sign-In
- Rewrite `LoginViewModel` in `commonMain` to use `AuthService` -- removes Activity dependency

**This is the most complex platform-specific piece.**

### Phase 7: Navigation Migration

- Move `AppRoute` to `commonMain`, remove `NavKey` interface
- Create simple `Navigator` class in `commonMain` (mutableStateListOf-based backstack)
- Remove Navigation3 dependencies

### Phase 8: UI Layer to commonMain

- Move all Screen composables to `commonMain`
- Replace `hiltViewModel()` with `koinViewModel()`
- Remove `stringResource(R.string.default_web_client_id)` -- pass web client ID through AuthService
- Remove `LocalContext.current` / Activity casts -- handled by AuthService
- Theme: move `Color.kt`, `Type.kt` to `commonMain`; create `expect/actual` for `HabitTrackerTheme` (Android: dynamic color, iOS: static scheme)
- Create root `App.kt` composable with theme + navigator + screen routing
- Simplify `MainActivity.kt` to just `setContent { App() }`

### Phase 9: iOS Entry Point

- Configure iOS targets in `build.gradle.kts` (iosArm64, iosSimulatorArm64, iosX64 with framework output)
- Create `MainViewController.kt` in `iosMain`: `fun MainViewController() = ComposeUIViewController { App() }`
- Create Xcode project (`iosApp/`) with SwiftUI wrapper hosting `ComposeUIViewController`
- Add Firebase iOS SDK via SPM, add `GoogleService-Info.plist`
- Add Google Sign-In iOS SDK via SPM (if using GIDSignIn)
- Call `FirebaseApp.configure()` and `KoinHelper.initKoin()` from Swift `App.init()`

---

## Potential Challenges

1. **Room KMP on iOS** -- requires `instantiateImpl()` generated extension and KSP running for all iOS targets
2. **Google Sign-In on iOS** -- no Credential Manager equivalent; need GIDSignIn SDK or pivot to Apple Sign-In
3. **Firebase iOS linking** -- GitLive wraps the native Firebase iOS SDK, which must be linked separately in Xcode via SPM
4. **SwipeToDismissBox on iOS** -- verify gesture handling works in CMP; may need platform-specific fallback
5. **Compose on iOS performance** -- CMP renders via Skiko/Skia; test scrolling with many item

---

## Verification

1. **Android**: build (`./gradlew assembleDebug`) and run after each phase -- must never break
2. **iOS**: after Phase 9, build from Xcode, verify app launches in iOS Simulator
3. **Functional tests**: Google Sign-In flow on both platforms, CRUD habits, swipe-to-complete, daily reset logic, Firestore sync
4. **Unit tests**: run `./gradlew test` -- existing tests should pass (update DI setup in tests from Hilt to Koin)
