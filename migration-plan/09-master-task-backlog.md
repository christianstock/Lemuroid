# Master Implementation Task Backlog

## Overview & Tracking

This document contains a prioritized, dependency-linked checklist of implementation tasks for executing the Lemuroid Flutter clean-room migration focused on **Game Boy (GB, GBC, GBA via mGBA)** and **RetroAchievements integration**, with **Android TV removed**.

Each task has a stable identifier (`TASK-001` onwards), explicit dependencies, relative effort estimate (XS–XL), and verifiable acceptance criteria.

---

## Task Backlog Summary Matrix

```
Phase 0: Spikes (TASK-001 -> TASK-003)
  └── Phase 1: Foundation (TASK-004 -> TASK-010)
        ├── Phase 2: mGBA Emulation Kernel (TASK-011 -> TASK-020)
        ├── Phase 3: GB/GBC/GBA Library & SAF (TASK-021 -> TASK-030)
        ├── Phase 4: RetroAchievements, Saves & Cheats (TASK-031 -> TASK-040)
        └── Phase 5: Handheld Controls & Release (TASK-041 -> TASK-050)
```

---

## Detailed Task Specifications

### Phase 0: Discovery & Technical Spikes

#### `TASK-001`: GLRetroView mGBA AndroidView Embedding Spike
- **Phase**: Phase 0
- **Feature**: `emulation_session`
- **Dependencies**: None
- **Effort**: **M**
- **Description**: Build minimal Flutter proof-of-concept app embedding `GLRetroView` with native `mgba.so` core inside an `AndroidView` platform view factory to verify 60 FPS rendering.
- **Acceptance Criteria**: Test GBA ROM renders at 60 FPS without black screens or memory leaks on Activity pause/resume.

#### `TASK-002`: RetroAchievements `rcheevos` C Memory Inspection Spike
- **Phase**: Phase 0
- **Feature**: `retro_achievements`
- **Dependencies**: None
- **Effort**: **M**
- **Description**: Verify that `rcheevos` C library can inspect mGBA RAM addresses without degrading frame pacing.
- **Acceptance Criteria**: Native callback triggers event channel call when specific mGBA RAM address changes value.

#### `TASK-003`: SAF Bulk GBA Directory Scan Spike
- **Phase**: Phase 0
- **Feature**: `game_library`
- **Dependencies**: None
- **Effort**: **S**
- **Description**: Benchmarks native Android batch tree content querying vs Dart SAF reading for 500+ GBA ROMs.
- **Acceptance Criteria**: Native Kotlin SAF worker returns file array payload across platform channel in < 3 seconds.

---

### Phase 1: Core Foundation & Architecture Shell

#### `TASK-004`: Initialize Flutter Application Structure & Theme
- **Phase**: Phase 1
- **Feature**: `shared`
- **Dependencies**: `TASK-001`
- **Effort**: **S**
- **Description**: Set up target Flutter project, directory tree, Material 3 dark theme, and Riverpod `ProviderScope`.
- **Acceptance Criteria**: App compiles cleanly (`flutter analyze`) and launches shell.

#### `TASK-005`: Configure go_router Navigation Stack
- **Phase**: Phase 1
- **Feature**: `shared`
- **Dependencies**: `TASK-004`
- **Effort**: **S**
- **Description**: Implement route map (`/`, `/library`, `/emulation_session`, `/retro_achievements`, `/settings`).
- **Acceptance Criteria**: Route navigation functions cleanly with back-stack support.

#### `TASK-006`: Implement Drift SQLite Database Schema
- **Phase**: Phase 1
- **Feature**: `shared/database`
- **Dependencies**: `TASK-004`
- **Effort**: **M**
- **Description**: Create Drift tables (`games`, `data_files`, `game_cheats`, `achievements`) mirroring legacy Room `RetrogradeDatabase`.
- **Acceptance Criteria**: Drift code generation creates type-safe DAOs; database initializes successfully.

#### `TASK-007`: Implement Room Database Migration Handler
- **Phase**: Phase 1
- **Feature**: `shared/database`
- **Dependencies**: `TASK-006`
- **Effort**: **M**
- **Description**: Write SQLite schema migration opener converting existing `retrograde.db` files without data loss.
- **Acceptance Criteria**: Unit tests confirm legacy seed SQLite DB opens and imports games cleanly.

#### `TASK-008`: Implement Shared App Logger & Error Handler
- **Phase**: Phase 1
- **Feature**: `core`
- **Dependencies**: `TASK-004`
- **Effort**: **XS**
- **Description**: Build unified logging utility piping Dart logs to logcat in debug mode.
- **Acceptance Criteria**: Log statements filtered by severity level.

#### `TASK-009`: Implement App Preferences Store
- **Phase**: Phase 1
- **Feature**: `settings`
- **Dependencies**: `TASK-004`
- **Effort**: **S**
- **Description**: Create `SharedPreferencesWithCache` wrapper for settings (screen filter, low-latency audio, RetroAchievements login).
- **Acceptance Criteria**: Preference state persists across app restarts.

#### `TASK-010`: Set Up ARB Localizations (`l10n`)
- **Phase**: Phase 1
- **Feature**: `shared`
- **Dependencies**: `TASK-004`
- **Effort**: **S**
- **Description**: Port existing Android `strings.xml` resources into `app_en.arb` and `app_es.arb`.
- **Acceptance Criteria**: Strings correctly localized using `AppLocalizations.of(context)`.

---

### Phase 2: mGBA Emulation Kernel & Touch Skins

#### `TASK-011`: Build Native GLRetroView mGBA Platform View Factory
- **Phase**: Phase 2
- **Feature**: `emulation_session`
- **Dependencies**: `TASK-001`, `TASK-004`
- **Effort**: **M**
- **Description**: Write Kotlin `GLRetroViewFactory` registering `com.swordfish.lemuroid/GLRetroView` loading `mgba.so`.
- **Acceptance Criteria**: Platform view renders mGBA surface correctly inside Flutter widget tree.

#### `TASK-012`: Implement mGBA Emulation Command MethodChannel
- **Phase**: Phase 2
- **Feature**: `emulation_session`
- **Dependencies**: `TASK-011`
- **Effort**: **M**
- **Description**: Implement `MethodChannel` for `initializeCoreAndGame`, `pauseEmulation`, `resumeEmulation`, and `unloadGame`.
- **Acceptance Criteria**: Dart calls trigger corresponding native mGBA C++ actions.

#### `TASK-013`: Implement Emulation EventStream Channel
- **Phase**: Phase 2
- **Feature**: `emulation_session`
- **Dependencies**: `TASK-012`
- **Effort**: **S**
- **Description**: Stream native events (`onFrameRendered`, `onCoreError`, `onRumble`) to Dart subscribers.
- **Acceptance Criteria**: Dart receives live frame events and native errors asynchronously.

#### `TASK-014`: Build EmulationSessionNotifier Riverpod Controller
- **Phase**: Phase 2
- **Feature**: `emulation_session`
- **Dependencies**: `TASK-012`, `TASK-013`
- **Effort**: **M**
- **Description**: Create state notifier managing session lifecycle (`Uninitialized` -> `Loading` -> `Running` -> `Paused`).
- **Acceptance Criteria**: UI updates seamlessly based on emulation session state.

#### `TASK-015`: Build GB, GBC & GBA CustomPainter Touch Skins
- **Phase**: Phase 2
- **Feature**: `controller_input`
- **Dependencies**: `TASK-014`
- **Effort**: **L**
- **Description**: Reimplement virtual DPAD, A/B buttons, and L/R shoulder triggers using Flutter `CustomPainter`.
- **Acceptance Criteria**: Multi-touch input sends correct Libretro bitmasks to mGBA core.

#### `TASK-016`: Build Touch Control Aspect Ratio & Skin Engine
- **Phase**: Phase 2
- **Feature**: `controller_input`
- **Dependencies**: `TASK-015`
- **Effort**: **M**
- **Description**: Port system-specific layouts for GB (1:1 aspect ratio), GBC, and GBA (3:2 aspect ratio).
- **Acceptance Criteria**: Touch overlays scale dynamically to screen orientation and system configuration.

#### `TASK-017`: Implement Physical Gamepad Input Manager
- **Phase**: Phase 2
- **Feature**: `controller_input`
- **Dependencies**: `TASK-014`
- **Effort**: **M**
- **Description**: Intercept physical gamepad `KeyEvent` and `MotionEvent` inputs and route to mGBA `retro_set_input_state`.
- **Acceptance Criteria**: Connected Bluetooth or handheld controller buttons navigate and play instantly.

#### `TASK-018`: Implement In-Game Overlay Menu
- **Phase**: Phase 2
- **Feature**: `emulation_session`
- **Dependencies**: `TASK-014`
- **Effort**: **M**
- **Description**: Build floating menu with Pause, Fast-Forward, Reset, Save State, and Exit actions.
- **Acceptance Criteria**: In-game menu pauses mGBA emulation smoothly without frame corruption.

#### `TASK-019`: Implement Hardware Motion/Tilt Bridge
- **Phase**: Phase 2
- **Feature**: `emulation_session`
- **Dependencies**: `TASK-013`
- **Effort**: **S**
- **Description**: Bind device accelerometer sensors for tilt-compatible GBC/GBA ROMs (e.g., *Yoshi's Universal Gravitation*).
- **Acceptance Criteria**: Tilting device moves in-game analog tilt controls accurately.

#### `TASK-020`: Implement Device Haptic Rumble Controller
- **Phase**: Phase 2
- **Feature**: `emulation_session`
- **Dependencies**: `TASK-013`
- **Effort**: **S**
- **Description**: Trigger device vibration upon receiving mGBA rumble callbacks (e.g., *Pokemon Pinball*).
- **Acceptance Criteria**: Haptic feedback triggers accurately during rumble events.

---

### Phase 3: GB/GBC/GBA Game Library & Storage Engine

#### `TASK-021`: Implement Native SAF Fast Directory Reader
- **Phase**: Phase 3
- **Feature**: `shared/storage`
- **Dependencies**: `TASK-003`
- **Effort**: **M**
- **Description**: Build native Kotlin SAF tree reader querying child documents in batch over platform channel.
- **Acceptance Criteria**: Returns complete directory tree payload for 500+ GBA content URIs in < 3 seconds.

#### `TASK-022`: Implement Dart GBA/GBC ROM Header Serial Extractor
- **Phase**: Phase 3
- **Feature**: `game_library`
- **Dependencies**: `TASK-006`
- **Effort**: **M**
- **Description**: Port header serial parser to pure Dart, reading game codes (`AGB-XXXX`) from GBA/GBC byte streams.
- **Acceptance Criteria**: Correctly extracts serial strings from mock GBA/GBC ROM files.

#### `TASK-023`: Implement Async Libretro `.rdb` Database Parser
- **Phase**: Phase 3
- **Feature**: `game_library`
- **Dependencies**: `TASK-022`
- **Effort**: **M**
- **Description**: Port Libretro binary database parser into Dart bytes reader for matching GB/GBC/GBA CRC32 serials.
- **Acceptance Criteria**: Matches serials against `.rdb` files to retrieve official title and cover artwork URL.

#### `TASK-024`: Implement StorageScanner Orchestrator
- **Phase**: Phase 3
- **Feature**: `game_library`
- **Dependencies**: `TASK-021`, `TASK-022`, `TASK-023`
- **Effort**: **L**
- **Description**: Coordinate directory scanning, serial parsing, and batch committing scanned entries into Drift DB.
- **Acceptance Criteria**: Progress notifier emits scanning percentage; records written cleanly to SQLite.

#### `TASK-025`: Implement Game Library Repository & Queries
- **Phase**: Phase 3
- **Feature**: `game_library`
- **Dependencies**: `TASK-006`, `TASK-024`
- **Effort**: **M**
- **Description**: Build repository supplying streams for Recent Games, Favorites, and System Categories (GB, GBC, GBA).
- **Acceptance Criteria**: Reactive streams reflect live database updates immediately.

#### `TASK-026`: Implement Library Home Screen UI
- **Phase**: Phase 3
- **Feature**: `game_library`
- **Dependencies**: `TASK-025`
- **Effort**: **M**
- **Description**: Build Material 3 tabbed library screen with system filter pills (GB, GBC, GBA) and responsive grid.
- **Acceptance Criteria**: Displays cover art tiles with title and system indicators.

#### `TASK-027`: Implement Game Cover Image Loader & Cache
- **Phase**: Phase 3
- **Feature**: `shared`
- **Dependencies**: `TASK-026`
- **Effort**: **S**
- **Description**: Build image loader using `cached_network_image` with local placeholder fallback.
- **Acceptance Criteria**: Smooth scrolling grid without memory jank during cover art loading.

#### `TASK-028`: Implement Game Search & Filter Bar
- **Phase**: Phase 3
- **Feature**: `game_library`
- **Dependencies**: `TASK-025`
- **Effort**: **S**
- **Description**: Build live search input bar filtering game list by title or system ID.
- **Acceptance Criteria**: Search updates catalog view dynamically with debounced query state.

#### `TASK-029`: Implement Game Details Dialog & Launcher Sheet
- **Phase**: Phase 3
- **Feature**: `game_library`
- **Dependencies**: `TASK-026`, `TASK-014`
- **Effort**: **M**
- **Description**: Build modal sheet showing game cover, system name, favorite toggle, and Launch button.
- **Acceptance Criteria**: Tapping Launch transitions smoothly to `emulation_session` route.

#### `TASK-030`: Implement Storage Directory Picker Sheet
- **Phase**: Phase 3
- **Feature**: `game_library`
- **Dependencies**: `TASK-021`
- **Effort**: **S**
- **Description**: Build folder selection sheet allowing users to pick GBA/GBC ROM storage directories.
- **Acceptance Criteria**: Selected SAF URI saved to preferences and triggers rescan.

---

### Phase 4: RetroAchievements, Saves & Cheats

#### `TASK-031`: Implement RetroAchievements API Client & Authenticator
- **Phase**: Phase 4
- **Feature**: `retro_achievements`
- **Dependencies**: `TASK-006`, `TASK-009`
- **Effort**: **M**
- **Description**: Authenticate RetroAchievements user credentials with `https://retroachievements.org/API/` and store token.
- **Acceptance Criteria**: User logs in successfully; authentication token persists securely.

#### `TASK-032`: Implement RetroAchievements Game Hash Calculator
- **Phase**: Phase 4
- **Feature**: `retro_achievements`
- **Dependencies**: `TASK-031`
- **Effort**: **M**
- **Description**: Compute RetroAchievements MD5 checksum for GB, GBC, and GBA ROMs and fetch achievement definition payload.
- **Acceptance Criteria**: Returns matching game ID and achievement manifest from RetroAchievements API.

#### `TASK-033`: Implement Native `rcheevos` JNI Integration & Event Bridge
- **Phase**: Phase 4
- **Feature**: `retro_achievements`
- **Dependencies**: `TASK-002`, `TASK-032`
- **Effort**: **L**
- **Description**: Wire native `rcheevos` C library to inspect mGBA RAM addresses and emit `onAchievementUnlocked` event channel events.
- **Acceptance Criteria**: In-memory achievement conditions trigger unlock payload across event channel.

#### `TASK-034`: Implement In-Game Achievement Unlock Toast Banner Overlay
- **Phase**: Phase 4
- **Feature**: `retro_achievements`
- **Dependencies**: `TASK-033`
- **Effort**: **M**
- **Description**: Render animated, non-intrusive achievement unlock toast banner with badge image, title, and points over gameplay.
- **Acceptance Criteria**: Banner animates onto screen smoothly during active mGBA gameplay and dismisses after 4 seconds.

#### `TASK-035`: Implement Achievement Catalog Sheet & Badge Cache
- **Phase**: Phase 4
- **Feature**: `retro_achievements`
- **Dependencies**: `TASK-031`
- **Effort**: **M**
- **Description**: Sheet displaying list of achievements for current game with locked/unlocked indicators and cached badge icons.
- **Acceptance Criteria**: Displays user achievement progress and points total.

#### `TASK-036`: Implement Save States Repository & File Manager
- **Phase**: Phase 4
- **Feature**: `save_states`
- **Dependencies**: `TASK-012`, `TASK-014`
- **Effort**: **M**
- **Description**: Manage 10 save slots (0–9) per game for mGBA, auto-save state generator, and SRAM flusher.
- **Acceptance Criteria**: Generates correct pathing (`.state0`–`.state9`) and invokes native state saves.

#### `TASK-037`: Implement Save State Preview Screenshot Store
- **Phase**: Phase 4
- **Feature**: `save_states`
- **Dependencies**: `TASK-036`
- **Effort**: **M**
- **Description**: Capture OpenGL frame preview PNG and store alongside save state files.
- **Acceptance Criteria**: Screenshot preview thumbnails display accurately in slot picker sheet.

#### `TASK-038`: Implement Save Slot Picker Bottom Sheet UI
- **Phase**: Phase 4
- **Feature**: `save_states`
- **Dependencies**: `TASK-037`
- **Effort**: **M**
- **Description**: Interactive modal sheet displaying save slots with screenshot previews, timestamps, and Save/Load actions.
- **Acceptance Criteria**: Selecting slot loads state instantly; overwrite triggers confirmation prompt.

#### `TASK-039`: Implement GameShark / ActionReplay Cheat Parser
- **Phase**: Phase 4
- **Feature**: `game_cheats`
- **Dependencies**: `TASK-012`
- **Effort**: **M**
- **Description**: Port GameShark / ActionReplay code parser to Dart, sending cheat strings via platform channel to mGBA.
- **Acceptance Criteria**: Toggling cheat code updates mGBA RAM values without freezing core.

#### `TASK-040`: Implement GBA BIOS Hash Validator (gba_bios.bin)
- **Phase**: Phase 4
- **Feature**: `bios_manager`
- **Dependencies**: `TASK-006`
- **Effort**: **S**
- **Description**: Compute MD5 checksum of user-provided `gba_bios.bin` and validate for official GBA boot animation.
- **Acceptance Criteria**: Validates official GBA BIOS hash (`a860e8c0b6d573d191e4ec7db1b1e4f6`).

---

### Phase 5: Handheld Controls, Benchmarking & Release Candidate

#### `TASK-041`: Implement Touch Controller Customizer Editor UI
- **Phase**: Phase 5
- **Feature**: `controller_input`
- **Dependencies**: `TASK-015`, `TASK-016`
- **Effort**: **M**
- **Description**: Drag-and-drop editor allowing resizing, repositioning, and opacity adjustment of virtual GB/GBA buttons.
- **Acceptance Criteria**: Custom touch layout positions save to preferences and apply to gameplay overlay.

#### `TASK-042`: Implement Custom Gamepad Key Remapping Screen
- **Phase**: Phase 5
- **Feature**: `controller_input`
- **Dependencies**: `TASK-017`
- **Effort**: **M**
- **Description**: Key remapping screen listening to physical handheld button presses (Retroid/Odin) and saving key bindings.
- **Acceptance Criteria**: Custom physical button mappings persist and control emulation accurately.

#### `TASK-043`: Implement App Settings Screen
- **Phase**: Phase 5
- **Feature**: `settings`
- **Dependencies**: `TASK-009`
- **Effort**: **M**
- **Description**: Settings tiles for screen filters, low-latency audio, RetroAchievements account settings, and theme selection.
- **Acceptance Criteria**: Modifying settings updates app configuration immediately.

#### `TASK-044`: Implement Background Task Worker for Rescan
- **Phase**: Phase 5
- **Feature**: `shared`
- **Dependencies**: `TASK-024`
- **Effort**: **S**
- **Description**: Configure `workmanager` Flutter plugin to schedule periodic ROM library rescans in background.
- **Acceptance Criteria**: Background rescan updates database without user intervention.

#### `TASK-045`: Execute mGBA 60 FPS Performance Benchmarking
- **Phase**: Phase 5
- **Feature**: `core`
- **Dependencies**: All Phase 0–4 tasks
- **Effort**: **M**
- **Description**: Measure FPS, frame render latency, input latency, and battery draw during active GBA gameplay.
- **Acceptance Criteria**: Sustained 60 FPS output across target GBA games; touch input latency < 8 ms.

#### `TASK-046`: Execute RetroAchievements Integration Test Suite
- **Phase**: Phase 5
- **Feature**: `retro_achievements`
- **Dependencies**: `TASK-033`, `TASK-034`
- **Effort**: **M**
- **Description**: Execute integration tests verifying user authentication, MD5 hash calculation, and RAM unlock callbacks.
- **Acceptance Criteria**: 100% test pass rate for RetroAchievements login and unlock triggers.

#### `TASK-047`: Execute Automated Unit & Database Migration Tests
- **Phase**: Phase 5
- **Feature**: `core`
- **Dependencies**: All Phase 0–4 tasks
- **Effort**: **M**
- **Description**: Run unit and widget tests; verify database migration from legacy seed files.
- **Acceptance Criteria**: All unit and widget tests pass cleanly.

#### `TASK-048`: Configure Build Flavors (Bundle vs Dynamic)
- **Phase**: Phase 5
- **Feature**: `core`
- **Dependencies**: `TASK-004`
- **Effort**: **S**
- **Description**: Set up Gradle build flavors (`bundle` with pre-packaged `mgba.so` vs `dynamic` with core downloader).
- **Acceptance Criteria**: `flutter build apk --flavor bundle` packages `mgba.so`.

#### `TASK-049`: Package Pre-Compiled `mgba.so` Binaries
- **Phase**: Phase 5
- **Feature**: `core`
- **Dependencies**: `TASK-048`
- **Effort**: **S**
- **Description**: Verify inclusion of optimized `mgba.so` binaries for `arm64-v8a`, `armeabi-v7a`, and `x86_64` ABIs.
- **Acceptance Criteria**: All target ABIs execute mGBA core flawlessly.

#### `TASK-050`: Build & Sign Production Release Candidate APK
- **Phase**: Phase 5
- **Feature**: `core`
- **Dependencies**: `TASK-045`, `TASK-046`, `TASK-047`, `TASK-048`, `TASK-049`
- **Effort**: **S**
- **Description**: Generate signed production release APK and verify installation on physical Android handheld hardware.
- **Acceptance Criteria**: Release APK compiles, installs, and executes error-free.
