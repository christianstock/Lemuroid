# Target Architecture & Directory Design

## 1. High-Level Architectural Vision

The target architecture for **GameGrl** is built on a **Feature-First Clean Architecture** using custom handheld UI components (scrapping standard Google Material Design), mGBA emulation kernel, real-time frame rewind, fast-forward, hardware ambient light sensor integration, LCD/CRT display shaders, PDF game manual viewer, 3D front/back box art, and RetroAchievements integration.

```
                  +-----------------------------------+
                  |      GameGrl Declarative Router   |
                  +-----------------+-----------------+
                                    |
          +-------------------------+-------------------------+
          |                                                   |
+---------v----------+                             +----------v---------+
| Feature: Game Lib  |                             | Feature: Manuals   |
|  - Custom Carousel |                             |  - PDF Viewer      |
|  - Game Info Card  |                             |  - Page Zoom       |
|  - 3D Box Art      |                             +----------+---------+
+---------+----------+                                        |
          |                                                   |
          +-------------------------+-------------------------+
                                    |
                  +-----------------v-----------------+
                  |    Feature: Emulation Session     |
                  |  - mGBA Platform View             |
                  |  - Real-Time Rewind Controller    |
                  |  - Fast-Forward Multiplier        |
                  |  - Light Sensor (Boktai ID 6)     |
                  |  - In-Game Pause Menu             |
                  |  - LCD / CRT Shader Selector      |
                  +-----------------+-----------------+
                                    |
                  +-----------------v-----------------+
                  |   Feature: RetroAchievements      |
                  |  - rcheevos Memory Observer       |
                  |  - Hardcore Mode Enforcer         |
                  |  - Unlock Banner Toast Overlay    |
                  +-----------------+-----------------+
                                    |
                  +-----------------v-----------------+
                  |      Native Android Plugin        |
                  |  - LibretroDroid (mGBA JNI)       |
                  |  - Light Sensor (Illuminance 6)   |
                  |  - Rewind Buffer & EGL Shaders    |
                  +-----------------------------------+
```

---

## 2. Proposed Repository Directory Structure

```
gamegrl_flutter/
├── android/                             # Android Native Platform Project
│   └── app/src/main/
│       ├── cpp/                         # Native C/C++
│       │   ├── libretrodroid.cpp        # Libretro JNI & Rewind buffer engine
│       │   └── rcheevos/                # Native RetroAchievements C library
│       └── kotlin/com/swordfish/lemuroid/
│           ├── platform_view/           # GLRetroView AndroidView Factory
│           ├── sensors/                 # Light Sensor (TYPE_LIGHT -> ID 6) Listener
│           └── channels/                # MethodChannel & EventChannel handlers
├── lib/
│   ├── main.dart                        # Entry point & app initialization
│   ├── app.dart                         # GameGrl Root & Theme Provider
│   │
│   ├── features/                        # FEATURE DOMAINS
│   │   ├── game_library/                # Custom GameGrl Handheld UI
│   │   │   ├── domain/                  # Game, DataFile, SystemID (GB, GBC, GBA)
│   │   │   ├── presentation/            #
│   │   │   │   ├── gamegrl_carousel_view.dart  # Custom 3D Coverflow Carousel
│   │   │   │   ├── game_info_card.dart         # Custom Game Details Card
│   │   │   │   ├── box_art_3d_flipper.dart     # Front & Back Box Art Flipper
│   │   │   │   └── custom_games_view.dart      # Custom Games Catalog View
│   │   │   ├── state/                   # LibraryNotifier, ScanProgressNotifier
│   │   │   └── data/                    # StorageScanner, DriftGameDao
│   │   │
│   │   ├── emulation_session/           # Active mGBA gameplay & session recovery
│   │   │   ├── domain/                  # GameLoader, RewindBufferConfig
│   │   │   ├── presentation/            #
│   │   │   │   ├── game_screen.dart            # Fullscreen mGBA Surface
│   │   │   │   ├── in_game_pause_menu.dart     # Quick-save, quick-load & reset
│   │   │   │   └── rewind_overlay_button.dart  # Hold-to-rewind touch button
│   │   │   ├── state/                   # EmulationSessionNotifier, RewindNotifier
│   │   │   └── data/                    # NativeEmulationBridge, NativeSensorBridge
│   │   │
│   │   ├── game_manuals/                # Embedded Game Manuals
│   │   │   ├── presentation/            # pdf_manual_viewer_screen.dart
│   │   │   └── data/                    # ManualRepository
│   │   │
│   │   ├── shader_engine/               # EGL LCD & CRT Display Shaders
│   │   │   ├── presentation/            # lcd_shader_picker_sheet.dart
│   │   │   └── data/                    # ShaderRepository
│   │   │
│   │   ├── theme_engine/                # Custom GameGrl Themes & Palettes
│   │   │   ├── presentation/            # gameboy_palette_picker_sheet.dart
│   │   │   └── state/                   # ThemePaletteNotifier
│   │   │
│   │   ├── retro_achievements/          # RetroAchievements.org integration
│   │   │   ├── presentation/            # AchievementListSheet, UnlockBannerOverlay
│   │   │   ├── state/                   # RetroAchievementsNotifier
│   │   │   └── data/                    # RetroAchievementsApiClient, RcheevosBridge
│   │   │
│   │   └── save_states/                 # Save & restore state slots
│   │       ├── presentation/            # SaveSlotPickerSheet
│   │       └── data/                    # SaveStatesRepository, ScreenshotStore
│   │
│   ├── shared/                          # SHARED CROSS-CUTTING INFRASTRUCTURE
│   │   ├── database/                    # Drift SQLite database schema
│   │   ├── storage/                     # SAF Framework & Cache Cleaner
│   │   └── theme/                       # Custom GameGrl Handheld Styling
│   │
│   └── core/                            # CORE CONTRACTS & LOGGING
│       └── logger.dart
└── test/                                # UNIT, WIDGET & INTEGRATION TESTS
```

---

## 3. Platform Channels & Native JNI Command Matrix

### 3.1 Command Channel (`com.swordfish.lemuroid/emulation_command`)
- `initializeCoreAndGame(dataMap)` -> Loads `mgba.so` and ROM path.
- `setRewindEnabled(enabled, bufferSizeMb)` -> Allocates native frame rewind buffer (disabled automatically in Hardcore Mode).
- `rewindStep(stepsCount)` -> Rewinds mGBA state backward by N frames.
- `setFastForwardRatio(multiplier)` -> Fast-forwards mGBA emulation (1.5x–8x).
- `sendSensorEvent(sensorId: 6, value: lux)` -> Forwards hardware light sensor reading to mGBA solar sensor (*Boktai*).
- `setShader(shaderName)` -> Loads EGL GLSL shader preset (`lcd-grid.glsl`, `crt-easymode.glsl`).
- `initRetroAchievements(username, token, gameHash)` -> Initializes `rcheevos` memory observer.

---

## 4. Drift SQLite Persistence Schema

```dart
// Table: games (Updated for Front/Back Box Art & Manuals)
class Games extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get title => text()();
  TextColumn get path => text()();
  TextColumn get systemId => text()(); // "gb", "gbc", "gba"
  TextColumn get coverUrl => text().nullable()(); // Front Box Art
  TextColumn get backCoverUrl => text().nullable()(); // Back Box Art Scan
  TextColumn get manualUrl => text().nullable()(); // PDF Manual Path
  BooleanColumn get isFavorite => boolean().withDefault(const Constant(false))();
}
```
