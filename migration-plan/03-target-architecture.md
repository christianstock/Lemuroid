# Target Architecture & Directory Design

## 1. High-Level Architectural Vision

The target architecture for the Flutter reimplementation of Lemuroid is built on a **Feature-First Clean Architecture** tailored for high-performance Game Boy (GB, GBC, GBA) emulation and RetroAchievements integration.

Rather than arranging files by layer (`adapters`, `views`, `viewmodels`), the codebase is divided into **autonomous feature modules**, each containing its presentation, domain, state management, and data access layers.

```
                  +-----------------------------------+
                  |      Flutter Navigation (Router)  |
                  +-----------------+-----------------+
                                    |
          +-------------------------+-------------------------+
          |                                                   |
+---------v----------+                             +----------v---------+
| Feature: Emulation |                             | Feature: Game Lib  |
|  - View (Widget)   |                             |  - View (Catalog)  |
|  - Controller      |                             |  - mGBA Scanner    |
|  - mGBA Session    |                             |  - Drift DB Query  |
+---------+----------+                             +----------+---------+
          |                                                   |
          +-------------------------+-------------------------+
                                    |
                  +-----------------v-----------------+
                  |   Feature: RetroAchievements      |
                  |  - User Auth & Hardcore Mode      |
                  |  - Achievement Unlock Banners     |
                  |  - rcheevos C Memory Observer     |
                  +-----------------+-----------------+
                                    |
                  +-----------------v-----------------+
                  |      Native Android Plugin        |
                  |  - LibretroDroid (mGBA JNI)       |
                  |  - GLRetroView Platform View      |
                  |  - rcheevos C Library Integration |
                  +-----------------------------------+
```

---

## 2. Proposed Repository Directory Structure

```
lemuroid_flutter/
├── android/                             # Android Native Platform Project
│   └── app/src/main/
│       ├── cpp/                         # Native C/C++
│       │   ├── libretrodroid.cpp        # Libretro JNI engine
│       │   └── rcheevos/                # Native RetroAchievements C library
│       └── kotlin/com/swordfish/lemuroid/
│           ├── platform_view/           # GLRetroView AndroidView Factory
│           ├── channels/                # MethodChannel & EventChannel handlers
│           └── storage/                 # Native SAF fast tree reader
├── lib/
│   ├── main.dart                        # Entry point & app initialization
│   ├── app.dart                         # MaterialApp, Theme & Router root
│   │
│   ├── features/                        # FEATURE DOMAINS
│   │   ├── emulation_session/           # Active mGBA gameplay & Libretro lifecycle
│   │   │   ├── domain/                  # GameLoader, mGBACoreOption models
│   │   │   ├── presentation/            # GameScreen, GLRetroViewContainer, OverlayMenu
│   │   │   ├── state/                   # EmulationSessionNotifier
│   │   │   └── data/                    # NativeEmulationBridge, CoreVariablesRepo
│   │   │
│   │   ├── retro_achievements/          # RetroAchievements.org integration
│   │   │   ├── domain/                  # Achievement, UserProfile, Leaderboard models
│   │   │   ├── presentation/            # AchievementListSheet, UnlockBannerOverlay
│   │   │   ├── state/                   # RetroAchievementsNotifier
│   │   │   └── data/                    # RetroAchievementsApiClient, RcheevosBridge
│   │   │
│   │   ├── game_library/                # GB/GBC/GBA ROM scanning, catalog & search
│   │   │   ├── domain/                  # Game, DataFile, SystemID (GB, GBC, GBA)
│   │   │   ├── presentation/            # LibraryHomeScreen, GameDetailsDialog
│   │   │   ├── state/                   # LibraryNotifier, RomScanProgressNotifier
│   │   │   └── data/                    # StorageScanner, LibretroDbParser, DriftGameDao
│   │   │
│   │   ├── save_states/                 # Save & restore state management
│   │   │   ├── domain/                  # SaveStateSlot, SaveInfo models
│   │   │   ├── presentation/            # SaveSlotPickerSheet
│   │   │   ├── state/                   # SaveStatesNotifier
│   │   │   └── data/                    # SaveStatesRepository, ScreenshotStore
│   │   │
│   │   ├── controller_input/            # GB/GBC/GBA touch skins & physical gamepads
│   │   │   ├── domain/                  # InputBinding, RetroKey, SkinLayout
│   │   │   ├── presentation/            # TouchControllerOverlay, KeyBindingScreen
│   │   │   ├── state/                   # InputBindingNotifier, TouchSkinNotifier
│   │   │   └── data/                    # InputDeviceManager, LayoutPreferences
│   │   │
│   │   ├── game_cheats/                 # GB/GBA GameShark / ActionReplay cheats
│   │   │   ├── domain/                  # Cheat, GameSharkCode models
│   │   │   ├── presentation/            # CheatManagerSheet
│   │   │   ├── state/                   # CheatsNotifier
│   │   │   └── data/                    # CheatParser, CheatRepository
│   │   │
│   │   ├── bios_manager/                # GBA BIOS verification (gba_bios.bin)
│   │   │   ├── domain/                  # BiosFile, BiosStatus models
│   │   │   ├── presentation/            # BiosStatusScreen
│   │   │   └── data/                    # BiosCheckerRepository
│   │   │
│   │   └── settings/                    # Global app & mGBA options
│   │       ├── presentation/            # SettingsScreen
│   │       └── state/                   # AppSettingsNotifier
│   │
│   ├── shared/                          # SHARED CROSS-CUTTING INFRASTRUCTURE
│   │   ├── database/                    # Drift SQLite database schema & migrations
│   │   ├── storage/                     # SAF Storage Access Framework & file utilities
│   │   ├── theme/                       # Handheld dark theme, Game Boy palettes
│   │   ├── utils/                       # Streams, Crypto hashes, HTTP
│   │   └── widgets/                     # Generic buttons, dialogs, image cache
│   │
│   └── core/                            # CORE CONTRACTS & LOGGING
│       ├── platform_channel_keys.dart   # String constants for channel calls
│       └── logger.dart                  # Unified logging wrapper
└── test/                                # UNIT, WIDGET & INTEGRATION TESTS
    ├── features/                        # Feature tests (including RetroAchievements mocks)
    └── mocks/                           # Mocktail / Platform Channel mocks
```

---

## 3. Strict Dependency Governance Rules

1. **Feature Independence**: Features must **NEVER** import internal implementation classes from other features.
   - *Allowed*: `emulation_session` listening to `retro_achievements` unlock events via public Stream contracts.
2. **Presentation Unidirectional Flow**: Widgets -> Notifiers -> Repositories -> Native Channels / SQLite.
3. **Platform Channel Isolation**: Method and Event Channels wrapped exclusively within `data/` repositories.

---

## 4. State Management Architecture (Riverpod)

The application uses **Flutter Riverpod** (v2.x with code generation) for state management:

### State Categories & Isolation

| State Category | Lifecycle | Tool / Class | Description |
| :--- | :--- | :--- | :--- |
| **UI Presentation State** | Short-lived | `StateProvider` / `Notifier` | Bottom sheet visibility, active tab, search filter. |
| **Durable Application State**| Long-lived | `AsyncNotifier` + Drift DB | GB/GBC/GBA catalog, controller mappings, settings. |
| **RetroAchievements State** | User Session | `RetroAchievementsNotifier` | User login token, Hardcore mode, game achievement list. |
| **Emulator Runtime State** | Session-bound | `EmulationSessionNotifier` | Native JNI lifecycle, mGBA FPS, save state slots. |

---

## 5. Native Interoperability & RetroAchievements Engine

Communication between Flutter Dart, native mGBA, and RetroAchievements (`rcheevos`) flows through three channels:

### 5.1 Command Channel (`com.swordfish.lemuroid/emulation_command`)
- `initializeCoreAndGame(dataMap)` -> Loads `mgba.so` and ROM path.
- `initRetroAchievements(username, token, gameHash)` -> Initializes `rcheevos` memory observer.
- `pauseEmulation()` / `resumeEmulation()` -> Controls mGBA execution.
- `saveState(slotIndex)` / `loadState(slotIndex)` -> Executes state serialization.

### 5.2 Event Channel (`com.swordfish.lemuroid/emulation_events`)
- `onAchievementUnlocked(achievementId, title, points, badgeUrl)` -> Triggers in-game overlay unlock banner.
- `onRumbleEvent(strength)` -> Triggers haptics.
- `onCoreError(code, message)` -> Signals JNI failures.

### 5.3 Surface View (`GLRetroView`)
Embedded via Flutter `AndroidView` (Hybrid Composition), providing direct EGL frame rendering.

---

## 6. Persistence & Schema Design (Drift SQLite)

To preserve database backward compatibility for `retrograde.db`:

```dart
// Table: games
class Games extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get title => text()();
  TextColumn get path => text()();
  TextColumn get systemId => text()(); // "gb", "gbc", "gba"
  TextColumn get coverUrl => text().nullable()();
  BooleanColumn get isFavorite => boolean().withDefault(const Constant(false))();
}

// Table: achievements (RetroAchievements local cache)
class Achievements extends Table {
  IntColumn get id => integer()();
  IntColumn get gameId => integer().references(Games, #id)();
  TextColumn get title => text()();
  TextColumn get description => text()();
  IntColumn get points => integer()();
  TextColumn get badgeUrl => text()();
  BooleanColumn get isUnlocked => boolean().withDefault(const Constant(false))();
  IntColumn get unlockTimestamp => integer().nullable()();
}
```

---

## 7. Mobile & Handheld Layout Strategy

The UI is optimized specifically for mobile screens and handheld gaming devices (e.g., Retroid, Odin):

1. **Touch Controls**: Customizable GB, GBC, and GBA touch skins with adjustable button sizing and transparency.
2. **Physical Gamepad Remapping**: Complete mapping for physical DPAD, face buttons (A, B, X, Y), and shoulder buttons (L, R).
3. **Immersive Mode**: Fullscreen rendering hiding Android system navigation bars during gameplay.
