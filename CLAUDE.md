# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Test
./gradlew test                    # Unit tests (JVM)
./gradlew connectedAndroidTest    # Instrumented tests (requires device/emulator)

# Lint
./gradlew lint

# Clean
./gradlew clean
```

## Firebase Setup (required before first build)

1. Create a Firebase project and add an Android app with package `com.learnkmp.habittrackerandroid`
2. Enable **Google Sign-In** in Firebase Auth and **Cloud Firestore** in the Firebase console
3. Download `google-services.json` and place it at `app/google-services.json`
4. The `google-services` Gradle plugin auto-generates `R.string.default_web_client_id` from that file, which `LoginScreen` reads at runtime

## Architecture

Single-module app using **MVVM + Repository pattern** with Hilt DI.

**Stack:** Kotlin 2.2.10, Jetpack Compose (BOM 2026.02.01), Material Design 3, Hilt 2.56, Room 2.7.1, Firebase Auth + Firestore, Credential Manager (Google Sign-In), Nav3 (`androidx.navigation3`).

### Package layout

```
com.learnkmp.habittrackerandroid/
├── HabitTrackerApp.kt          @HiltAndroidApp Application
├── MainActivity.kt             @AndroidEntryPoint; hosts the Nav3 NavDisplay
├── navigation/AppRoutes.kt     Sealed AppRoute: Login | HabitList | CreateHabit
├── domain/model/Habit.kt       Pure domain model (no Android/Room imports)
├── data/
│   ├── local/                  Room: HabitEntity, HabitDao, HabitDatabase, Mappers
│   ├── remote/                 FirestoreHabitDataSource (manual field mapping)
│   └── repository/             HabitRepository interface + HabitRepositoryImpl
├── di/                         DatabaseModule, FirebaseModule, RepositoryModule
└── ui/
    ├── auth/                   LoginScreen + LoginViewModel (Credential Manager flow)
    ├── habitlist/              HabitListScreen + HabitListViewModel
    └── createhabit/            CreateHabitScreen + CreateHabitViewModel
```

### Navigation (Nav3)

Nav3 is **not** Nav2. There is no `NavController` or `NavHost`. The entire navigation state is a `SnapshotStateList<AppRoute>` from `rememberNavBackStack`. `NavDisplay` maps each route to a `NavEntry`. Navigate by calling `backStack.add(route)` or `backStack.removeLastOrNull()` directly.

### Data flow

- Room is the single source of truth for the UI (observed via `Flow`).
- On login, `HabitRepositoryImpl` fetches all Firestore documents and upserts them into Room.
- All writes go to Room immediately, then are mirrored to Firestore (fire-and-forget).
- `completedToday` resets automatically via `Mappers.toDomain()`: if `lastCompleedDate != today` the habit is treated as not completed.

### Dependency management

All versions live in `gradle/libs.versions.toml`. Add new libraries there, not inline in build files.

**Note:** KSP version must match the Kotlin version (`ksp = "2.2.10-x.y.z"`). Check the [KSP releases](https://github.com/google/ksp/releases) if the build fails with a KSP version mismatch.

### Coding conventions

- **No hardcoded strings for Firestore field names or collection paths.** Use `const val` constants (typically in a `companion object`) instead. See `FirestoreHabitDataSource` for the pattern.

### MCP tools

Make sure to use the mcp tools for testing and diagnostics
