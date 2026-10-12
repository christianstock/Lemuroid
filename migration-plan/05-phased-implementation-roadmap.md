# Phased Implementation Roadmap

## 1. Roadmap Overview & Milestones

The migration is organized into **six incremental phases (Phase 0 through Phase 5)**.

Each phase produces a verifiable, working milestone. Rather than attempting a big-bang rewrite, this vertical-slice approach delivers a runnable application early, progressively replacing mock components with full native capabilities.

```
+-------------------------------------------------------------------------------+
| Phase 0: Discovery & Technical Spikes                                         |
|  - PoC: GLRetroView inside AndroidView                                        |
|  - PoC: SAF Fast Directory Reader Channel                                     |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 1: Core Foundation & Shell App                                          |
|  - Flutter Skeleton & go_router setup                                         |
|  - Drift Database Schema & Room Migration                                     |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 2: Native Emulation Bridge & Gameplay Kernel                            |
|  - Platform View (AndroidView) + MethodChannel JNI Commands                   |
|  - Touch Overlay Controls & Gamepad Event Routing                             |
|  * RUNNABLE MILESTONE: Launch raw ROM into active emulation session           |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 3: Game Library, Storage Engine & Metadata                              |
|  - SAF Directory Scanner & Libretro DB Parser                                 |
|  - Library Home Screen, Cover Loader & Search                                 |
|  * RUNNABLE MILESTONE: Full ROM discovery, indexing & launch workflow        |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 4: Save States, Cheats & Core Configuration                             |
|  - Save State Slots (0-9) & Preview Screenshot Store                          |
|  - Cheat Downloader, Parser & Live Toggling                                   |
|  - Core Variables & Bios Validator                                            |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 5: Android TV, Polish & Release Candidate                               |
|  - D-Pad Focus Navigation & TV Channels                                       |
|  - Performance Benchmarking (60 FPS verification)                             |
|  * RELEASE CANDIDATE MILESTONE: Production-ready APK                          |
+-------------------------------------------------------------------------------+
```

---

## 2. Granular Phase Breakdown

### Phase 0: Discovery & Risk Reduction (Technical Spikes)

#### Work Item 0.1: GLRetroView AndroidView Embedding Spike
- **Objective**: Prove that `GLRetroView` can render Libretro OpenGL ES frames inside a Flutter `AndroidView` with 60 FPS performance and zero black-screen deadlocks.
- **Prerequisites**: Android SDK, Flutter SDK setup.
- **Implementation Steps**:
  1. Create minimal Flutter app with native Android platform view factory for `GLRetroView`.
  2. Embed a test static core (e.g., Gambatte or Snes9x) and launch a test ROM.
  3. Measure frame rendering rate and memory overhead using Android Profiler.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Test ROM runs continuously at 60 FPS in Flutter `AndroidView` with touch input response.

#### Work Item 0.2: Storage Access Framework (SAF) Performance Spike
- **Objective**: Compare Dart SAF tree reading against native Kotlin `DocumentFile` batch querying to determine SAF channel requirements.
- **Effort Estimate**: **S** (Confidence: High)
- **Acceptance Criteria**: Benchmarked URI query rate exceeding 100 ROM files/second.

---

### Phase 1: Core Foundation & Architecture Shell

#### Work Item 1.1: Flutter Repository Setup & Core Contracts
- **Objective**: Establish Flutter project structure, Riverpod dependency graph, Material 3 theme, and `go_router` paths.
- **New Components**: `lib/main.dart`, `lib/app.dart`, `lib/core/logger.dart`, `lib/shared/theme/app_theme.dart`.
- **Effort Estimate**: **S** (Confidence: High)
- **Acceptance Criteria**: Clean compilation with `flutter analyze` passing and basic shell navigation operating.

#### Work Item 1.2: Database Layer & Room Schema Migration
- **Objective**: Implement Drift SQLite schema mirroring legacy Room `RetrogradeDatabase` tables (`games`, `data_files`, `game_cheats`).
- **Existing Files**: `RetrogradeDatabase.kt`, `GameDao.kt`, `Migrations.kt`.
- **New Components**: `lib/shared/database/app_database.dart`, `lib/shared/database/database_migrations.dart`.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Successful SQLite database initialization and schema migration test verifying zero data loss from existing `retrograde.db` files.

---

### Phase 2: Native Emulation Bridge & Gameplay Kernel

#### Work Item 2.1: Native MethodChannel & EventChannel Bridge
- **Objective**: Build JNI bridge between Dart `EmulationSessionNotifier` and native `LibretroDroid`.
- **Existing Files**: `GameViewModelRetroGameView.kt`, `LibretroDroid.java`.
- **New Components**:
  - `android/.../channels/EmulationCommandChannel.kt`
  - `lib/features/emulation_session/data/native_emulation_bridge.dart`
- **Effort Estimate**: **L** (Confidence: Medium)
- **Acceptance Criteria**: Reliable execution of `loadGame`, `pauseEmulation`, `resumeEmulation`, and `unloadGame` over platform channels.

#### Work Item 2.2: On-Screen Touch Controller Overlay & Skins
- **Objective**: Rebuild virtual touch overlays (DPAD, analog sticks, action buttons) in Flutter using `CustomPainter`.
- **Existing Files**: `lemuroid-touchinput/` submodule, `PspLandscapeSkin.kt`, `GameBoyAdvancePortraitSkin.kt`.
- **New Components**: `lib/features/controller_input/presentation/touch_controller_overlay.dart`.
- **Effort Estimate**: **L** (Confidence: High)
- **Acceptance Criteria**: Touch gestures correctly transmit Libretro input bits to native JNI engine with low latency.

---

### Phase 3: Game Library, Storage Engine & Metadata

#### Work Item 3.1: ROM Directory Scanner & Libretro DB Parser
- **Objective**: Recursively scan local directories and SAF trees, parse header serials, and query Libretro `.rdb` binary database files.
- **Existing Files**: `LemuroidLibrary.kt`, `LocalStorageProvider.kt`, `LibretroDbParser.kt`.
- **New Components**: `lib/features/game_library/data/scanner/storage_scanner.dart`, `lib/features/game_library/data/scanner/libretro_db_parser.dart`.
- **Effort Estimate**: **L** (Confidence: High)
- **Acceptance Criteria**: Scanner indexes 500+ ROMs into Drift SQLite database with correct title, system ID, and serial matching.

#### Work Item 3.2: Library UI Home Screen & Cover Art Loader
- **Objective**: Construct responsive game catalog UI featuring recent games, system filtering, search, and cover thumbnail caching.
- **Existing Files**: Compose library views in `lemuroid-app/src/main/java/.../mobile/feature/library/`.
- **New Components**: `lib/features/game_library/presentation/library_home_screen.dart`, `lib/shared/widgets/game_cover_image.dart`.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Smooth scrolling grid displaying cover art; tapping game launches active emulation session.

---

### Phase 4: Save States, Cheats & Advanced Features

#### Work Item 4.1: Save State Manager & Screenshot Preview Store
- **Objective**: Manage 10 save slots (0–9) per game, trigger auto-saves on app pause, and capture state preview screenshots.
- **Existing Files**: `SavesManager.kt`, `StatesManager.kt`, `StatesPreviewManager.kt`.
- **New Components**: `lib/features/save_states/data/save_states_repository.dart`, `lib/features/save_states/presentation/save_slot_picker_sheet.dart`.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: States saved and restored reliably; save previews displayed in slot picker sheet; verified cross-compatibility with legacy `.state` files.

#### Work Item 4.2: Cheat Engine & CwCheat Parser
- **Objective**: Download Libretro cheat archives, parse `.cht` and CwCheat format strings, and allow toggling cheats in-game.
- **Existing Files**: `CheatManager.kt`, `CwCheatParser.kt`.
- **New Components**: `lib/features/game_cheats/data/cheat_repository.dart`, `lib/features/game_cheats/presentation/cheat_manager_sheet.dart`.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: In-game cheat sheet allows toggling codes; native JNI updates memory values without crashing.

---

### Phase 5: Android TV, Polish & Release Candidate

#### Work Item 5.1: Android TV D-Pad Focus Navigation & TV Channels
- **Objective**: Ensure entire Flutter UI can be navigated using a hardware D-pad / gamepad without touch input; update Android TV home screen channels.
- **Existing Files**: `MainTVActivity.kt`, `ChannelHandler.kt`.
- **New Components**: `lib/features/tv_navigation/presentation/tv_focus_wrapper.dart`.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: 100% of screens navigable via D-pad; focused elements clearly highlighted with TV selection glow.

#### Work Item 5.2: Final Benchmarking, Regression & Release Build
- **Objective**: Execute automated test suite, verify 60 FPS frame timing across target systems, test database migration on upgraded devices, and produce release APK.
- **Effort Estimate**: **S** (Confidence: High)
- **Acceptance Criteria**: Zero regression errors; all acceptance tests passing; release build signed and verified on physical hardware.

---

## 3. Relative Effort Summary Matrix

| Phase | Work Item | Focus Area | Effort | Confidence |
| :--- | :--- | :--- | :---: | :---: |
| **Phase 0** | 0.1 | GLRetroView AndroidView Embedding Spike | **M** | High |
| **Phase 0** | 0.2 | SAF Directory Reader Spike | **S** | High |
| **Phase 1** | 1.1 | Flutter Architecture & Shell Setup | **S** | High |
| **Phase 1** | 1.2 | Drift Database Schema & Room Migration | **M** | High |
| **Phase 2** | 2.1 | Native JNI MethodChannel Bridge | **L** | Medium |
| **Phase 2** | 2.2 | CustomPainter Touch Overlay Controls | **L** | High |
| **Phase 3** | 3.1 | SAF Storage Scanner & Libretro RDB Parser | **L** | High |
| **Phase 3** | 3.2 | Game Library UI & Cover Image Caching | **M** | High |
| **Phase 4** | 4.1 | Save States Slot Manager & Previews | **M** | High |
| **Phase 4** | 4.2 | Cheat Downloader & Parser Engine | **M** | High |
| **Phase 5** | 5.1 | Android TV D-Pad Focus Navigation | **M** | High |
| **Phase 5** | 5.2 | Performance Benchmarking & Release Build | **S** | High |
