# Target Architecture & Directory Design

## 1. High-Level Architectural Vision

The target architecture for **GameGrl** is built on a **Feature-First Clean Architecture** using custom handheld UI components (scrapping standard Google Material Design), mGBA emulation kernel, real-time frame rewind, LCD/CRT display shaders, PDF game manual viewer, 3D front/back box art, and RetroAchievements integration.

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
                  |  - In-Game Pause & State Slot Menu|
                  |  - LCD / CRT Shader Selector      |
                  +-----------------+-----------------+
                                    |
                  +-----------------v-----------------+
                  |   Feature: RetroAchievements      |
                  |  - rcheevos Memory Observer       |
                  |  - Unlock Banner Toast Overlay    |
                  +-----------------+-----------------+
                                    |
                  +-----------------v-----------------+
                  |      Native Android Plugin        |
                  |  - LibretroDroid (mGBA JNI)       |
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
│   │   │   └── data/                    # NativeEmulationBridge, RewindController
│   │   │
│   │   ├── game_manuals/                # Embedded Game Manuals
│   │   │   ├── domain/                  # ManualDocument, PageBookmark
│   │   │   ├── presentation/            # pdf_manual_viewer_screen.dart
│   │   │   └── data/                    # ManualRepository
│   │   │
│   │   ├── shader_engine/               # EGL LCD & CRT Display Shaders
│   │   │   ├── domain/                  # ShaderPreset (LCD Grid, Dot Matrix, CRT)
│   │   │   ├── presentation/            # lcd_shader_picker_sheet.dart
│   │   │   └── data/                    # ShaderRepository
│   │   │
│   │   ├── theme_engine/                # Custom GameGrl Themes & Palettes
│   │   │   ├── domain/                  # GameBoyPalette (DMG Green, Pocket Gray, etc.)
│   │   │   ├── presentation/            # gameboy_palette_picker_sheet.dart
│   │   │   └── state/                   # ThemePaletteNotifier
│   │   │
│   │   ├── retro_achievements/          # RetroAchievements.org integration
│   │   │   ├── domain/                  # Achievement, UserProfile models
│   │   │   ├── presentation/            # AchievementListSheet, UnlockBannerOverlay
│   │   │   ├── state/                   # RetroAchievementsNotifier
│   │   │   └── data/                    # RetroAchievementsApiClient, RcheevosBridge
│   │   │
│   │   ├── save_states/                 # Save & restore state slots
│   │   │   ├── domain/                  # SaveStateSlot, SaveInfo
│   │   │   ├── presentation/            # SaveSlotPickerSheet
│   │   │   └── data/                    # SaveStatesRepository, ScreenshotStore
│   │   │
│   │   ├── controller_input/            # Touch skins & handheld gamepads
│   │   │   ├── domain/                  # InputBinding, RetroKey, SkinLayout
│   │   │   ├── presentation/            # TouchControllerOverlay, KeyBindingScreen
│   │   │   └── data/                    # InputDeviceManager
│   │   │
│   │   └── game_cheats/                 # GB/GBA GameShark / ActionReplay cheats
│   │       ├── presentation/            # CheatManagerSheet
│   │       └── data/                    # CheatParser, CheatRepository
│   │
│   ├── shared/                          # SHARED CROSS-CUTTING INFRASTRUCTURE
│   │   ├── database/                    # Drift SQLite database schema
│   │   ├── storage/                     # SAF Storage Access Framework
│   │   ├── theme/                       # Custom GameGrl Handheld Styling
│   │   └── widgets/                     # Custom buttons, dialogs, 3D cards
│   │
│   └── core/                            # CORE CONTRACTS & LOGGING
│       ├── platform_channel_keys.dart
│       └── logger.dart
└── test/                                # UNIT, WIDGET & INTEGRATION TESTS
```

---

## 3. State Management Architecture (Riverpod)

| State Category | Tool / Class | Description |
| :--- | :--- | :--- |
| **Custom UI Navigation** | `StateProvider` / `Notifier` | Active carousel index, Box Art flip angle, Manual page zoom. |
| **Game Catalog State** | `AsyncNotifier` + Drift DB | Scanned GB/GBC/GBA games with front & back cover artwork URIs. |
| **Rewind Engine State** | `RewindNotifier` | Active rewind buffer state, rewind speed multiplier (1x–4x). |
| **Display Shader State** | `ShaderNotifier` | Active EGL shader preset (`LCD Grid`, `LCD Dot Matrix`, `CRT`). |
| **Palette Theme State** | `ThemePaletteNotifier` | Active Game Boy LCD color palette (DMG Green, Pocket Gray, etc.). |
| **RetroAchievements State** | `RetroAchievementsNotifier` | User login token, Hardcore mode status, achievement progress. |

---

## 4. Platform Channels & Native JNI Command Matrix

### 4.1 Command Channel (`com.swordfish.lemuroid/emulation_command`)
- `initializeCoreAndGame(dataMap)` -> Loads `mgba.so` and ROM path.
- `setRewindEnabled(enabled, bufferSizeMb)` -> Allocates native frame rewind buffer.
- `rewindStep(stepsCount)` -> Rewinds mGBA state backward by N frames.
- `setShader(shaderName)` -> Loads EGL GLSL shader preset (`lcd-grid.glsl`, `crt-easymode.glsl`).
- `saveState(slotIndex)` / `loadState(slotIndex)` -> Executes state serialization.
- `initRetroAchievements(username, token, gameHash)` -> Initializes `rcheevos` memory observer.

---

## 5. Drift SQLite Persistence Schema

```dart
// Table: games (Updated for Front & Back Box Art)
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
