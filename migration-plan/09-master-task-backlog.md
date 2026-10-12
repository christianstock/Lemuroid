# Master Implementation Task Backlog

## Overview & Tracking

This document contains a prioritized, dependency-linked checklist of implementation tasks for executing the Lemuroid Flutter clean-room migration.

Each task has a stable identifier (`TASK-001` onwards), explicit dependencies, relative effort estimate (XS–XL), and verifiable acceptance criteria to guide developer or AI agent implementation.

---

## Task Backlog Summary Matrix

```
Phase 0: Spikes (TASK-001 -> TASK-003)
  └── Phase 1: Foundation (TASK-004 -> TASK-010)
        ├── Phase 2: Emulation Kernel (TASK-011 -> TASK-020)
        ├── Phase 3: Game Library & SAF (TASK-021 -> TASK-030)
        ├── Phase 4: Saves & Cheats (TASK-031 -> TASK-040)
        └── Phase 5: TV & Release (TASK-041 -> TASK-050)
```

---

## Detailed Task Specifications

### Phase 0: Discovery & Technical Spikes

#### `TASK-001`: GLRetroView AndroidView Embedding Spike
- **Phase**: Phase 0
- **Feature**: `emulation_session`
- **Dependencies**: None
- **Effort**: **M**
- **Description**: Build a minimal Flutter proof-of-concept app embedding `GLRetroView` inside an `AndroidView` platform view factory to verify 60 FPS OpenGL rendering and lifecycle resume.
- **Acceptance Criteria**: Test GBA/SNES ROM renders at 60 FPS without black screens or memory leaks on Activity pause/resume.

#### `TASK-002`: SAF Bulk Query Performance Spike
- **Phase**: Phase 0
- **Feature**: `game_library`
- **Dependencies**: None
- **Effort**: **S**
- **Description**: Benchmarks native Android batch tree content querying vs Dart SAF reading for 1,000+ files.
- **Acceptance Criteria**: Native Kotlin SAF worker returns file array payload across platform channel in < 5 seconds.

#### `TASK-003`: Touch Overlay Input Latency Spike
- **Phase**: Phase 0
- **Feature**: `controller_input`
- **Dependencies**: None
- **Effort**: **S**
- **Description**: Verify multi-touch gesture event dispatch overhead to native C++ JNI input buffers.
- **Acceptance Criteria**: Gesture-to-JNI latency measured under 8 ms (less than 1 frame).

---

### Phase 1: Core Foundation & Architecture Shell

#### `TASK-004`: Initialize Flutter Application Structure & Theme
- **Phase**: Phase 1
- **Feature**: `shared`
- **Dependencies**: `TASK-001`
- **Effort**: **S**
- **Description**: Set up target Flutter project, directory tree, Material 3 dark theme, and Riverpod `ProviderScope`.
- **Acceptance Criteria**: App compiles cleanly (`flutter analyze`) and launches Material 3 shell.

#### `TASK-005`: Configure go_router Navigation Stack
- **Phase**: Phase 1
- **Feature**: `shared`
- **Dependencies**: `TASK-004`
- **Effort**: **S**
- **Description**: Implement declarative route map (`/`, `/library`, `/emulation_session`, `/settings`, `/cheats`).
- **Acceptance Criteria**: Route navigation functions cleanly with back-stack support.

#### `TASK-006`: Implement Drift SQLite Database Schema
- **Phase**: Phase 1
- **Feature**: `shared/database`
- **Dependencies**: `TASK-004`
- **Effort**: **M**
- **Description**: Create Drift tables (`games`, `data_files`, `game_cheats`) mirroring legacy Room `RetrogradeDatabase`.
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
- **Description**: Create `SharedPreferencesWithCache` wrapper for settings (screen filter, HD mode, low-latency audio).
- **Acceptance Criteria**: Preference state persists across app restarts.

#### `TASK-010`: Set Up ARB Localizations (`l10n`)
- **Phase**: Phase 1
- **Feature**: `shared`
- **Dependencies**: `TASK-004`
- **Effort**: **S**
- **Description**: Port existing Android `strings.xml` resources into `app_en.arb` and `app_es.arb`.
- **Acceptance Criteria**: Strings correctly localized using `AppLocalizations.of(context)`.

---

### Phase 2: Native Emulation Bridge & Gameplay Kernel

#### `TASK-011`: Build Native GLRetroView AndroidView Factory
- **Phase**: Phase 2
- **Feature**: `emulation_session`
- **Dependencies**: `TASK-001`, `TASK-004`
- **Effort**: **L**
- **Description**: Write Kotlin `GLRetroViewFactory` registering `com.swordfish.lemuroid/GLRetroView` in Flutter plugin registry.
- **Acceptance Criteria**: Platform view renders correctly inside Flutter widget tree.

#### `TASK-012`: Implement Emulation Command MethodChannel
- **Phase**: Phase 2
- **Feature**: `emulation_session`
- **Dependencies**: `TASK-011`
- **Effort**: **M**
- **Description**: Implement `MethodChannel` for `initializeCoreAndGame`, `pauseEmulation`, `resumeEmulation`, and `unloadGame`.
- **Acceptance Criteria**: Dart calls trigger corresponding native `LibretroDroid` C++ actions.

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
- **Effort**: **L**
- **Description**: Create state notifier managing session lifecycle (`Uninitialized` -> `Loading` -> `Running` -> `Paused`).
- **Acceptance Criteria**: UI updates seamlessly based on emulation session state.

#### `TASK-015`: Build CustomPainter Touch Control Overlay
- **Phase**: Phase 2
- **Feature**: `controller_input`
- **Dependencies**: `TASK-003`, `TASK-014`
- **Effort**: **L**
- **Description**: Reimplement virtual DPAD, analog sticks, and action buttons using Flutter `CustomPainter`.
- **Acceptance Criteria**: Multi-touch input sends correct Libretro bitmasks to native core.

#### `TASK-016`: Build Touch Control Skin Layout Engine
- **Phase**: Phase 2
- **Feature**: `controller_input`
- **Dependencies**: `TASK-015`
- **Effort**: **M**
- **Description**: Port system-specific touch skins (GBA, PSX, PSP, NDS) with portrait and landscape layouts.
- **Acceptance Criteria**: Touch overlays dynamically scale to screen orientation and system configuration.

#### `TASK-017`: Implement Hardware Gamepad Input Manager
- **Phase**: Phase 2
- **Feature**: `controller_input`
- **Dependencies**: `TASK-014`
- **Effort**: **M**
- **Description**: Intercept physical gamepad `KeyEvent` and `MotionEvent` inputs and route to JNI `retro_set_input_state`.
- **Acceptance Criteria**: Connected Bluetooth gamepads control gameplay instantly.

#### `TASK-018`: Implement In-Game Overlay Menu
- **Phase**: Phase 2
- **Feature**: `emulation_session`
- **Dependencies**: `TASK-014`
- **Effort**: **M**
- **Description**: Build floating or gesture-triggered menu with Pause, Fast-Forward, Reset, and Exit actions.
- **Acceptance Criteria**: In-game menu pauses emulation smoothly without frame corruption.

#### `TASK-019`: Implement Hardware Motion/Tilt Controller Bridge
- **Phase**: Phase 2
- **Feature**: `emulation_session`
- **Dependencies**: `TASK-013`
- **Effort**: **M**
- **Description**: Bind device accelerometer/gyroscope sensors to native motion input sources.
- **Acceptance Criteria**: Tilting device moves in-game analog controls for motion-compatible ROMs.

#### `TASK-020`: Implement Device Haptic Rumble Controller
- **Phase**: Phase 2
- **Feature**: `emulation_session`
- **Dependencies**: `TASK-013`
- **Effort**: **S**
- **Description**: Trigger device vibration upon receiving native JNI rumble events.
- **Acceptance Criteria**: Haptic feedback triggers accurately during in-game rumble events.

---

### Phase 3: Game Library, Storage Engine & Metadata

#### `TASK-021`: Implement Native SAF Fast Directory Reader
- **Phase**: Phase 3
- **Feature**: `shared/storage`
- **Dependencies**: `TASK-002`
- **Effort**: **M**
- **Description**: Build native Kotlin SAF tree reader querying child documents in batch over platform channel.
- **Acceptance Criteria**: Returns complete directory tree payload for 1,000+ content URIs in < 3 seconds.

#### `TASK-022`: Implement Dart ROM Header Serial Extractor
- **Phase**: Phase 3
- **Feature**: `game_library`
- **Dependencies**: `TASK-006`
- **Effort**: **M**
- **Description**: Port `SerialScanner.kt` to pure Dart, reading binary headers for PSX, PSP, GBA, and NDS ROMs.
- **Acceptance Criteria**: Correctly extracts serial strings (e.g., `SLUS-00594`) from mock ROM files.

#### `TASK-023`: Implement Async Libretro `.rdb` Database Parser
- **Phase**: Phase 3
- **Feature**: `game_library`
- **Dependencies**: `TASK-022`
- **Effort**: **L**
- **Description**: Port Libretro binary database parser into Dart bytes reader for matching CRC32/serials.
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
- **Description**: Build repository supplying streams for Recent Games, Favorites, and System Categories.
- **Acceptance Criteria**: Reactive streams reflect live database updates immediately.

#### `TASK-026`: Implement Library Home Screen UI
- **Phase**: Phase 3
- **Feature**: `game_library`
- **Dependencies**: `TASK-025`
- **Effort**: **M**
- **Description**: Build Material 3 tabbed library screen with system filter pills and responsive grid.
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
- **Description**: Build folder selection sheet allowing users to pick ROM storage directories.
- **Acceptance Criteria**: Selected SAF URI saved to preferences and triggers rescan.

---

### Phase 4: Save States, Cheats & Advanced Features

#### `TASK-031`: Implement Save States Repository & File Manager
- **Phase**: Phase 4
- **Feature**: `save_states`
- **Dependencies**: `TASK-012`, `TASK-014`
- **Effort**: **M**
- **Description**: Manage 10 save slots (0–9) per game, auto-save state generator, and SRAM flusher.
- **Acceptance Criteria**: Generates correct pathing (`.state0`–`.state9`) and invokes native state saves.

#### `TASK-032`: Implement Save State Preview Screenshot Store
- **Phase**: Phase 4
- **Feature**: `save_states`
- **Dependencies**: `TASK-031`
- **Effort**: **M**
- **Description**: Capture OpenGL frame preview PNG and store alongside save state files.
- **Acceptance Criteria**: Screenshot preview thumbnails display accurately in slot picker sheet.

#### `TASK-033`: Implement Save Slot Picker Bottom Sheet UI
- **Phase**: Phase 4
- **Feature**: `save_states`
- **Dependencies**: `TASK-032`
- **Effort**: **M**
- **Description**: Interactive modal bottom sheet displaying save slots with screenshot previews, timestamps, and Save/Load actions.
- **Acceptance Criteria**: Selecting slot loads state instantly; overwrite triggers confirmation prompt.

#### `TASK-034`: Implement Auto-Save / Auto-Restore Engine
- **Phase**: Phase 4
- **Feature**: `save_states`
- **Dependencies**: `TASK-031`
- **Effort**: **M**
- **Description**: Automatically trigger state save on app backgrounding and offer auto-restore on game launch.
- **Acceptance Criteria**: Game session restores to exact frame where app was minimized.

#### `TASK-035`: Implement Cheat Database Zip Downloader
- **Phase**: Phase 4
- **Feature**: `game_cheats`
- **Dependencies**: `TASK-006`
- **Effort**: **M**
- **Description**: Download official Libretro cheat archive zip and extract `.cht` files to local storage.
- **Acceptance Criteria**: Successfully unzips and indexes system cheat files.

#### `TASK-036`: Implement CwCheat & `.cht` File Parser
- **Phase**: Phase 4
- **Feature**: `game_cheats`
- **Dependencies**: `TASK-035`
- **Effort**: **M**
- **Description**: Port `CwCheatParser.kt` regex parser to Dart, converting code strings to Libretro format.
- **Acceptance Criteria**: Extracted cheats parse description and code pairs accurately.

#### `TASK-037`: Implement In-Game Cheat Manager Sheet UI
- **Phase**: Phase 4
- **Feature**: `game_cheats`
- **Dependencies**: `TASK-036`, `TASK-012`
- **Effort**: **M**
- **Description**: In-game sheet allowing search, toggle, and custom cheat entry; sends `setCheat` commands via platform channel.
- **Acceptance Criteria**: Toggling cheat updates memory without freezing core.

#### `TASK-038`: Implement Core Variable Preferences Manager
- **Phase**: Phase 4
- **Feature**: `settings`
- **Dependencies**: `TASK-009`, `TASK-012`
- **Effort**: **M**
- **Description**: Store core-specific options (resolution scale, color depth, frame skip) and sync to JNI runtime on load.
- **Acceptance Criteria**: Modifying core variable takes effect immediately upon session reload.

#### `TASK-039`: Implement BIOS Hash Validator & File Manager
- **Phase**: Phase 4
- **Feature**: `bios_manager`
- **Dependencies**: `TASK-006`
- **Effort**: **M**
- **Description**: Compute MD5 checksums of files in `system/` directory and match against required core BIOS manifests.
- **Acceptance Criteria**: Identifies missing or invalid BIOS files and displays status list to user.

#### `TASK-040`: Implement Touch Controller Customizer Editor UI
- **Phase**: Phase 4
- **Feature**: `controller_input`
- **Dependencies**: `TASK-015`, `TASK-016`
- **Effort**: **L**
- **Description**: Interactive editor screen permitting drag-and-drop repositioning, scaling, and opacity adjustment of virtual buttons.
- **Acceptance Criteria**: Custom touch layout positions save to preferences and apply to gameplay overlay.

---

### Phase 5: Android TV, Polish & Release Candidate

#### `TASK-041`: Implement D-Pad Focus Traversal Policies for Android TV
- **Phase**: Phase 5
- **Feature**: `tv_navigation`
- **Dependencies**: `TASK-026`, `TASK-029`
- **Effort**: **L**
- **Description**: Wrap all UI screens in explicit directional `FocusTraversalGroup` nodes for hardware D-pad navigation.
- **Acceptance Criteria**: 100% of screens navigable via D-pad; focused elements display distinct selection highlight.

#### `TASK-042`: Implement TV Mode Auto-Detection & Layout Policy
- **Phase**: Phase 5
- **Feature**: `tv_navigation`
- **Dependencies**: `TASK-041`
- **Effort**: **S**
- **Description**: Query system UI mode and adapt spacing, font sizes, and focus highlights for TV display.
- **Acceptance Criteria**: Automatically activates TV layout when booted on Android TV device.

#### `TASK-043`: Implement Android TV Recommendations Channel Provider
- **Phase**: Phase 5
- **Feature**: `tv_navigation`
- **Dependencies**: `TASK-025`
- **Effort**: **M**
- **Description**: Write native Kotlin channel handler publishing recent games row to Android TV home screen.
- **Acceptance Criteria**: Recent games appear in Android TV launcher channels row.

#### `TASK-044`: Implement Custom Gamepad Key Remapping Screen
- **Phase**: Phase 5
- **Feature**: `controller_input`
- **Dependencies**: `TASK-017`
- **Effort**: **M**
- **Description**: Interactive key remapping screen listening to physical button presses and saving key bindings.
- **Acceptance Criteria**: Custom physical button mappings persist and control emulation accurately.

#### `TASK-045`: Implement App Settings & System Config Screen
- **Phase**: Phase 5
- **Feature**: `settings`
- **Dependencies**: `TASK-009`
- **Effort**: **M**
- **Description**: Material 3 settings tiles for screen filters, HD mode quality, low-latency audio, and theme selection.
- **Acceptance Criteria**: Modifying settings updates app configuration immediately.

#### `TASK-046`: Implement Background Task Worker for Rescan
- **Phase**: Phase 5
- **Feature**: `shared`
- **Dependencies**: `TASK-024`
- **Effort**: **S**
- **Description**: Configure `workmanager` Flutter plugin to schedule periodic ROM library rescans in background.
- **Acceptance Criteria**: Background rescan updates database without user intervention.

#### `TASK-047`: Execute Performance Benchmarking & Frame Timing Suite
- **Phase**: Phase 5
- **Feature**: `core`
- **Dependencies**: All Phase 0–4 tasks
- **Effort**: **M**
- **Description**: Measure FPS, frame render latency, input latency, and memory footprint on physical test hardware.
- **Acceptance Criteria**: Sustained 60 FPS across target cores; input latency under 12 ms; memory footprint < 150 MB.

#### `TASK-048`: Execute Automated Test Suite & Regression Checks
- **Phase**: Phase 5
- **Feature**: `core`
- **Dependencies**: All Phase 0–4 tasks
- **Effort**: **M**
- **Description**: Run unit, widget, and integration tests; verify database migration from legacy seed files.
- **Acceptance Criteria**: 100% test pass rate with zero regression errors.

#### `TASK-049`: Configure Build Flavors (Bundle vs Dynamic)
- **Phase**: Phase 5
- **Feature**: `core`
- **Dependencies**: `TASK-004`
- **Effort**: **S**
- **Description**: Set up Gradle build flavors (`bundle` with pre-packaged cores vs `dynamic` with GitHub core downloader).
- **Acceptance Criteria**: `flutter build apk --flavor bundle` packages all cores; `dynamic` variant builds lightweight APK.

#### `TASK-050`: Build & Sign Production Release Candidate APK
- **Phase**: Phase 5
- **Feature**: `core`
- **Dependencies**: `TASK-047`, `TASK-048`, `TASK-049`
- **Effort**: **S**
- **Description**: Generate signed production release APK and verify installation on physical Android hardware.
- **Acceptance Criteria**: Release APK compiles, installs, and executes all core features error-free.
