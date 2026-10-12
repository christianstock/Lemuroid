# Target Architecture & Directory Design

## 1. High-Level Architectural Vision

The target architecture for the Flutter reimplementation of Lemuroid is built on a **Feature-First Clean Architecture** tailored specifically for high-performance emulation applications.

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
|  - Controller      |                             |  - Scanner Repo    |
|  - Session State   |                             |  - Drift DB Query  |
+---------+----------+                             +----------+---------+
          |                                                   |
          +-------------------------+-------------------------+
                                    |
                  +-----------------v-----------------+
                  |       Shared Infrastructure       |
                  |  - Platform Channels Wrapper      |
                  |  - SAF Storage Engine             |
                  |  - Theme & Design System          |
                  +-----------------+-----------------+
                                    |
                  +-----------------v-----------------+
                  |      Native Android Plugin        |
                  |  - LibretroDroid JNI Bridge       |
                  |  - GLRetroView Platform View      |
                  |  - AudioTrack & Low-Latency Event |
                  +-----------------------------------+
```

---

## 2. Proposed Repository Directory Structure

```
lemuroid_flutter/
├── android/                             # Android Native Platform Project
│   └── app/src/main/
│       ├── cpp/                         # Native C/C++ (LibretroDroid JNI bindings)
│       └── kotlin/com/swordfish/lemuroid/
│           ├── platform_view/           # GLRetroView AndroidView Factory
│           ├── channels/                # MethodChannel & EventChannel handlers
│           └── storage/                 # Native SAF fast tree reader
├── lib/
│   ├── main.dart                        # Entry point & app initialization
│   ├── app.dart                         # MaterialApp, Theme & Router root
│   │
│   ├── features/                        # FEATURE DOMAINS
│   │   ├── emulation_session/           # Active gameplay & Libretro lifecycle
│   │   │   ├── domain/                  # GameLoader, CoreVariable models
│   │   │   ├── presentation/            # GameScreen, GLRetroViewContainer, InGameOverlay
│   │   │   ├── state/                   # EmulationSessionNotifier, FrameRateNotifier
│   │   │   └── data/                    # NativeEmulationBridge, CoreVariablesRepo
│   │   │
│   │   ├── game_library/                # ROM scanning, catalog & search
│   │   │   ├── domain/                  # Game, DataFile, GameSystem models
│   │   │   ├── presentation/            # LibraryHomeScreen, GameDetailsDialog, SystemGrid
│   │   │   ├── state/                   # LibraryNotifier, RomScanProgressNotifier
│   │   │   └── data/                    # StorageScanner, LibretroDbParser, DriftGameDao
│   │   │
│   │   ├── save_states/                 # Save & restore state management
│   │   │   ├── domain/                  # SaveStateSlot, SaveInfo models
│   │   │   ├── presentation/            # SaveSlotPickerSheet, StatesManagerDialog
│   │   │   ├── state/                   # SaveStatesNotifier
│   │   │   └── data/                    # SaveStatesRepository, ScreenshotStore
│   │   │
│   │   ├── controller_input/            # Touch skins & physical gamepads
│   │   │   ├── domain/                  # InputBinding, RetroKey, TouchLayoutConfig
│   │   │   ├── presentation/            # TouchControllerOverlay, KeyBindingScreen
│   │   │   ├── state/                   # InputBindingNotifier, TouchSkinNotifier
│   │   │   └── data/                    # InputDeviceManager, LayoutPreferences
│   │   │
│   │   ├── game_cheats/                 # Cheat database & live toggling
│   │   │   ├── domain/                  # Cheat, CwCheatCode models
│   │   │   ├── presentation/            # CheatManagerSheet, CheatImportDialog
│   │   │   ├── state/                   # CheatsNotifier
│   │   │   └── data/                    # CheatParser, CheatRepository
│   │   │
│   │   ├── bios_manager/                # BIOS verification & hash check
│   │   │   ├── domain/                  # BiosFile, BiosStatus models
│   │   │   ├── presentation/            # BiosStatusScreen
│   │   │   └── data/                    # BiosCheckerRepository
│   │   │
│   │   ├── settings/                    # Global app options
│   │   │   ├── presentation/            # SettingsScreen, SystemSettingsScreen
│   │   │   └── state/                   # AppSettingsNotifier
│   │   │
│   │   └── tv_navigation/               # Android TV D-Pad focus & Leanback channels
│   │       ├── presentation/            # TvFocusWrapper, TvHomeScreen
│   │       └── data/                    # AndroidTvChannelHandler
│   │
│   ├── shared/                          # SHARED CROSS-CUTTING INFRASTRUCTURE
│   │   ├── database/                    # Drift SQLite database schema & migrations
│   │   ├── storage/                     # Storage Access Framework & file utilities
│   │   ├── theme/                       # Typography, colors, TV focus themes
│   │   ├── utils/                       # Coroutines, Streams, Crypto hashes
│   │   └── widgets/                     # Generic buttons, dialogs, image cache loaders
│   │
│   └── core/                            # CORE CONTRACTS & LOGGING
│       ├── platform_channel_keys.dart   # String constants for channel calls
│       └── logger.dart                  # Unified logging wrapper
└── test/                                # UNIT, WIDGET & INTEGRATION TESTS
    ├── features/                        # Mirror feature tests
    ├── shared/                          # Shared unit tests
    └── mocks/                           # Mocktail / Platform Channel mocks
```

---

## 3. Strict Dependency Governance Rules

To preserve clean boundaries and avoid spaghetti coupling, the codebase enforces four core rules:

1. **Feature Independence**: Features must **NEVER** import internal implementation classes from other features.
   - *Allowed*: Feature A navigating to Feature B via `go_router` URI paths.
   - *Allowed*: Feature A reading shared domain entities (`Game`, `GameSystem`) defined in `shared/` or `core/`.
   - *Prohibited*: `features/emulation_session` importing `features/game_library/presentation/...`.

2. **Presentation Unidirectional Flow**:
   - `Presentation` depends on `State` / `Domain`.
   - `State` depends on `Domain` and `Data Repositories`.
   - `Domain` models depend on **nothing** (pure Dart code).
   - `Data` depends on `Domain` contracts and low-level storage/database drivers.

3. **Shared Infrastructure Isolation**:
   - Classes in `shared/` must **NEVER** depend on any `features/`.

4. **Platform Channel Isolation**:
   - Direct invocation of `MethodChannel` or `EventChannel` is restricted to dedicated repository classes in `data/` or `shared/storage/`. Widgets must never issue raw `MethodChannel.invokeMethod()` calls.

---

## 4. State Management Architecture (Riverpod)

The application uses **Flutter Riverpod** (v2.x with code generation) for state management:

### State Categories & Isolation

| State Category | Lifecycle | Tool / Class | Description |
| :--- | :--- | :--- | :--- |
| **UI Presentation State** | Short-lived (screen level) | `StateProvider` / `Notifier` | Dialog visibility, selected tab, temporary search query. |
| **Durable Application State** | Long-lived (persisted) | `AsyncNotifier` + `Drift` / `SharedPreferences` | ROM library catalog, controller bindings, global settings. |
| **Emulator Runtime State** | Session-bound | `EmulationSessionNotifier` | Native JNI lifecycle (`Uninitialized`, `Loading`, `Running`, `Paused`), FPS, audio state. |
| **Native Native/Hardware State**| Native process thread | C++ / JNI Memory | OpenGL texture context, Libretro RAM buffers, PCM audio track buffer. |

### Emulator Session Lifecycle Flow

```
[User Selects Game]
       |
       v
[EmulationSessionNotifier.startSession(Game)]
       |
       +---> 1. Fetch Game & Core SystemConfig from Drift DB
       +---> 2. Prepare System & Save Directories via StorageEngine
       +---> 3. Invoke MethodChannel: 'initializeCoreAndGame'
       |
       v
[Native Android Side: GLRetroView]
       |
       +---> 1. Load C++ .so Library via JNI
       +---> 2. Trigger retro_init() & retro_load_game()
       +---> 3. Start OpenGL render loop in SurfaceView
       |
       v
[EventChannel: 'onFrameRendered' / 'onRumble']
       |
       v
[EmulationSessionNotifier updates State to 'Running']
```

---

## 5. Native Interoperability & Platform Channels

Communication between Flutter Dart and the native Libretro C++ engine passes through three dedicated channel types:

### 5.1 Command Channel (`com.swordfish.lemuroid/emulation_command`)
`MethodChannel` handling synchronous/asynchronous control actions:
- `initializeCoreAndGame(dataMap)` -> Loads core `.so` and ROM path.
- `pauseEmulation()` / `resumeEmulation()` -> Controls `retro_run()` loop.
- `saveState(slotIndex, path)` / `loadState(slotIndex, path)` -> Invokes serialization.
- `setCheat(index, enabled, code)` -> Injects cheat string into Libretro core.
- `updateCoreVariables(variablesMap)` -> Modifies core option values.

### 5.2 Stream Channel (`com.swordfish.lemuroid/emulation_events`)
`EventChannel` broadcasting high-frequency native updates to Dart:
- `onRumbleEvent(strengthLeft, strengthRight)` -> Triggers device haptics.
- `onSensorRequest(sensorType, enabled)` -> Dynamically toggles motion listeners.
- `onCoreError(errorCode, message)` -> Signals unexpected rendering or JNI failures.

### 5.3 Platform View (`GLRetroView`)
Rendered via Flutter `AndroidView` using **Hybrid Composition** or **Texture Layer Hybrid Composition (TLHC)**:
```dart
Widget buildEmulationSurface(BuildContext context) {
  return AndroidView(
    viewType: 'com.swordfish.lemuroid/GLRetroView',
    layoutDirection: TextDirection.ltr,
    creationParams: creationParamsMap,
    creationParamsCodec: const StandardMessageCodec(),
    onPlatformViewCreated: (id) => controller.attachView(id),
  );
}
```

---

## 6. Persistence & Schema Compatibility

### 6.1 Database Schema (Drift SQLite)
To guarantee that existing Lemuroid users upgrading from Kotlin to Flutter experience zero data loss, the Drift SQLite schema mirrors the Room `RetrogradeDatabase`:

```dart
// Table: games (Mirrors Room 'Game' Entity)
class Games extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get title => text()();
  TextColumn get path => text()();
  TextColumn get systemId => text()();
  TextColumn get coverUrl => text().nullable()();
  BooleanColumn get isFavorite => boolean().withDefault(const Constant(false))();
  IntColumn get lastPlayedTimestamp => integer().nullable()();
}

// Table: data_files (Mirrors Room 'DataFile' Entity)
class DataFiles extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get gameId => integer().references(Games, #id)();
  TextColumn get uri => text()();
  TextColumn get checksum => text()();
}

// Table: game_cheats (Mirrors Room 'GameCheatEntity')
class GameCheats extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get gameId => integer().references(Games, #id)();
  IntColumn get cheatIndex => integer()();
  TextColumn get description => text()();
  TextColumn get code => text()();
  BooleanColumn get enabled => boolean().withDefault(const Constant(false))();
}
```

### 6.2 Directory Structure Compatibility
The Dart storage manager enforces identical relative paths for save files and states:
- Save RAM: `<AppDir>/saves/<SystemID>/<GameName>.srm`
- Save States: `<AppDir>/states/<SystemID>/<GameName>.state<Slot>`
- Core System Bios: `<AppDir>/system/<BiosName>`

---

## 7. Responsive Navigation & TV Focus Strategy

A single responsive codebase handles both mobile touchscreens and Android TV / handheld game consoles:

1. **Router**: `go_router` manages declartive route stacks (`/`, `/library`, `/game/:id`, `/settings`).
2. **TV Mode Auto-Detection**:
   - Queries `UiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION` or attached gamepad presence.
3. **D-Pad Focus Management**:
   - Wraps TV-interactive elements in custom `FocusableActionDetector` / `Focus` widgets.
   - Highlight animations and auto-scroll policies ensure seamless D-pad navigation without touch inputs.
