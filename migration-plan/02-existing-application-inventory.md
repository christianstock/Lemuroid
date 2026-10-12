# Existing Application Inventory & Disposition Analysis

## 1. Overview of Current Repository Architecture

The current repository is an Android Gradle multi-module project organized into technical layers and legacy subprojects:

- **`lemuroid-app`**: Main Android application module. Contains mobile Jetpack Compose UI screens, Android TV Leanback UI activities, ViewModels, WorkManager background workers, input event handlers, touch skins, and Dagger dependency injection graphs.
- **`retrograde-app-shared`**: Core business domain library. Defines Room database entities (`Game`, `GameCheatEntity`, `DataFile`), DAOs (`GameDao`, `GameCheatDao`), `GameLoader`, `SavesManager`, `StatesManager`, `BiosManager`, `CoreVariablesManager`, SAF file abstraction (`LocalStorageProvider`), and motion/rumble controllers.
- **`lemuroid-touchinput`**: On-screen touch controller layout engine and custom view logic (DPAD, analog sticks, radial action buttons, touch customization).
- **`lemuroid-metadata-libretro-db`**: Parser and indexer for Libretro binary metadata databases (`.dat`/`.rdb`), used to match ROM checksums (CRC32/MD5) with game titles, publisher, system, and cover art.
- **`retrograde-util`**: General-purpose Android utility classes and coroutine helpers.
- **`lemuroid-app-ext-free` / `lemuroid-app-ext-play`**: Flavor-specific extensions for core downloading (GitHub Releases vs Play Asset Delivery / Dynamic Feature Modules).
- **`baselineprofile`**: Android Macrobenchmark baseline profile generator.

---

## 2. Functional Inventory & Migration Dispositions

Each subsystem has been evaluated and assigned one of seven disposition tags:
1. **`Retain`**: Kept in native C/C++/Java/Kotlin code without structural rewrite.
2. **`Wrap`**: Encapsulated behind a clean Flutter Platform Channel API.
3. **`Reimplement`**: Rebuilt natively in Dart/Flutter.
4. **`Replace`**: Replaced with an idiomatic Flutter package or design pattern.
5. **`Simplify`**: Functionality preserved while eliminating unnecessary indirection.
6. **`Defer`**: Postponed to a secondary migration milestone.
7. **`Remove`**: Deliberately removed as obsolete or redundant.

---

### Inventory Breakdown

#### 2.1 Emulation Core Engine & Rendering Loop
- **Purpose**: Low-level Libretro core initialization, frame buffer rendering, audio output stream, game loading, and save RAM sync.
- **Existing Files**:
  - `LibretroDroid` library (`com.swordfish.libretrodroid.GLRetroView`, `GLRetroViewData`, `LibretroDroid`)
  - Native core libraries (`libretrodroid.so`, system `.so` cores)
  - `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/shared/game/viewmodel/GameViewModelRetroGameView.kt`
- **Domain Models**: `GLRetroViewData`, `VirtualFile`, `Variable`, `GameData`
- **Dependencies**: OpenGL ES 2.0/3.0, OpenSL ES / AudioTrack, JNI
- **Disposition**: **`Retain`** (Native C++ core and OpenGL surface) & **`Wrap`** (Exposed to Flutter via `AndroidView` platform view and MethodChannel).

---

#### 2.2 ROM Discovery & Storage Engine
- **Purpose**: Recursive filesystem and Android Storage Access Framework (SAF) scanning, zip archive unpacking, magic number detection, serial extraction, and file pairing.
- **Existing Files**:
  - `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/storage/*` (`LocalStorageProvider`, `StorageAccessFrameworkProvider`, `DocumentFileParser`, `MagicNumber`, `SerialScanner`, `RomFiles`)
- **Domain Models**: `StorageFile`, `GroupedStorageFiles`, `RomFiles`, `DataFile`
- **Dependencies**: Android `DocumentFile`, ContentResolver, Okio, ZipInputStream
- **Disposition**: **`Reimplement`** (Rebuilt in Dart using `dart:io` for local directories) & **`Wrap`** (MethodChannel calls to native SAF Kotlin helper for fast bulk URI queries).

---

#### 2.3 Game Library Database & Metadata Indexer
- **Purpose**: Local database persistence for scanned ROMs, system categorization, favorites, recent games, cover art downloads, and search.
- **Existing Files**:
  - `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/library/db/*` (`RetrogradeDatabase`, `GameDao`, `GameCheatDao`, `DataFileDao`)
  - `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/library/metadata/*` (`CompositeMetadataProvider`, `SkraperMetadataProvider`, `SkraperXmlParser`)
  - `lemuroid-metadata-libretro-db/*` (`LibretroDbParser`)
- **Domain Models**: `Game`, `DataFile`, `GameCheatEntity`, `GameSystem`, `SystemID`
- **Dependencies**: Android Room, SQLite, Retrofit, Coil
- **Disposition**: **`Replace`** (Room replaced by `drift` or `sqlite3` in Dart; Coil replaced by `cached_network_image`; Retrofit replaced by `dio` or `http`). Schema compatibility maintained for existing `retrograde.db`.

---

#### 2.4 Save State & Save File Management
- **Purpose**: Auto-save/auto-load on lifecycle pause, manual save state slots (slots 0–9), preview screenshot captures, state coherency checks, and legacy save migrators (e.g., MelonDS saves).
- **Existing Files**:
  - `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/saves/*` (`SavesManager`, `StatesManager`, `StatesPreviewManager`, `SavesCoherencyEngine`, `MelonDsSavesMigrator`)
  - `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/shared/game/viewmodel/GameViewModelSaves.kt`
- **Domain Models**: `SaveState`, `SaveInfo`, `IncompatibleStateException`
- **Dependencies**: Filesystem I/O, Libretro `.state` binary stream, Bitmap image compression
- **Disposition**: **`Reimplement`** (State slot management, screenshot preview loading, and coherency logic written in Dart; native byte array calls wrapped via MethodChannel).

---

#### 2.5 Input, Gamepad Mapping & Touch Controls
- **Purpose**: Virtual touch controls on mobile screens, touch layout customization (size/opacity/position), physics/analog sticks, rumble feedback, hardware gamepad input event routing, and custom button remapping.
- **Existing Files**:
  - `lemuroid-touchinput/*` (`TouchControllerCustomizer`, radial sensors)
  - `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/shared/input/*` (`InputDeviceManager`, `InputBindingUpdater`, `LemuroidInputDevice`, `RetroKey`)
  - `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/shared/game/skins/*` (Game Boy, GBA, PSP touch skin layouts)
- **Domain Models**: `ControllerConfig`, `RetroKey`, `InputClass`, `LemuroidInputDevice`
- **Dependencies**: Android `MotionEvent`, `KeyEvent`, VibrationManager
- **Disposition**: **`Reimplement`** (On-screen touch overlays built using Flutter `CustomPainter` and gesture detectors; Gamepad event handling routed through Flutter `Focus` / `HardwareKeyboard` or native KeyEvent channel).

---

#### 2.6 Cheat Engine & Cheat Parser
- **Purpose**: Downloading Libretro cheat database zips, parsing `.cht` and CwCheat format files, toggling cheats during runtime, and auto-detecting cheat-induced freezes.
- **Existing Files**:
  - `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/shared/cheats/*` (`CheatManager`, `CheatDownloader`, `CheatZipExplorer`, `CwCheatParser`, `CheatParser`)
  - `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/library/db/entity/GameCheatEntity.kt`
- **Domain Models**: `Cheat`, `GameCheatEntity`
- **Dependencies**: OkHttp, ZipInputStream, Room DB
- **Disposition**: **`Reimplement`** (Cheat parsing and UI managed in Dart; runtime cheat injection sent via `LibretroDroid.setCheat` platform channel calls).

---

#### 2.7 Core Option Variables & System Configuration
- **Purpose**: Customizing core-specific variables (e.g., rendering resolution, color depth, skip frames, audio latency, controller types).
- **Existing Files**:
  - `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/core/*` (`CoreVariablesManager`, `CoreVariable`, `CoresSelection`, `CoreUpdater`)
  - `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/shared/coreoptions/*` (`LemuroidCoreOption`, `CoreOptionsPreferenceHelper`)
- **Domain Models**: `CoreVariable`, `SystemCoreConfig`, `ExposedSetting`
- **Dependencies**: SharedPreferences / FlowPreferences
- **Disposition**: **`Reimplement`** (Dart preferences manager with type-safe core option schemas).

---

#### 2.8 BIOS & Firmware Management
- **Purpose**: Detecting missing BIOS files required by systems (e.g., PSX `scph5501.bin`, GBA `gba_bios.bin`, DS `bios7.bin`), validating MD5 hashes, and guiding users to provide BIOS files.
- **Existing Files**:
  - `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/bios/*` (`BiosManager`, `Bios`)
- **Domain Models**: `Bios`, `BiosStatus`
- **Dependencies**: Crypto MD5, File I/O
- **Disposition**: **`Reimplement`** (Dart BiosManager computing crypto hashes and checking system directory paths).

---

#### 2.9 Mobile & Android TV User Interfaces
- **Purpose**: Game catalog browsing, recent list, system lists, search, game detail dialogs, settings menus, in-game overlay menus, and Android TV Leanback home screen rows.
- **Existing Files**:
  - `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/*` (Jetpack Compose screens)
  - `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/tv/*` (Leanback Fragments & Activities)
- **Dependencies**: Jetpack Compose, Material3, Leanback UI
- **Disposition**: **`Simplify` & `Reimplement`**: Merged into a unified, responsive Flutter UI using `go_router`, auto-detecting TV/Gamepad mode to enable D-pad focus node navigation without code duplication.

---

#### 2.10 Cloud Save Synchronization
- **Purpose**: Background sync of game save files to Google Drive / cloud storage.
- **Existing Files**:
  - `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/savesync/*` (`SaveSyncManager`, `SaveSyncWork`)
- **Dependencies**: Google Drive API, WorkManager
- **Disposition**: **`Defer`** (Marked for Phase 5 / Post-MVP release to reduce initial migration risk).

---

#### 2.11 Background Task Scheduling & Notifications
- **Purpose**: Scheduled library rescanning, core updates, save sync, and cache cleaning via Android WorkManager.
- **Existing Files**:
  - `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/shared/library/LibraryIndexWork.kt`
  - `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/shared/storage/cache/CacheCleanerWork.kt`
- **Dependencies**: Android WorkManager
- **Disposition**: **`Wrap`** (Native Android WorkManager workers triggered via platform channels or lightweight Dart background tasks using `workmanager` Flutter package).

---

#### 2.12 Dependency Injection & Architecture Frameworks
- **Purpose**: Dagger 2 / Hilt dependency graphs, AndroidWorkerInjection, fragment factories.
- **Existing Files**:
  - `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/LemuroidApplicationComponent.kt`
  - Injection modules across subprojects.
- **Disposition**: **`Remove`** (Replaced by standard Dart compile-time state management and dependency injection using **Riverpod**).

---

## 3. Comprehensive Disposition Matrix Summary

| Subsystem / Feature | Current Location | Dependencies | Target Disposition | Target Implementation |
| :--- | :--- | :--- | :--- | :--- |
| **Libretro JNI Core Loader** | `LibretroDroid` C++ / JNI | OpenGL ES, Android Surface | **`Retain` & `Wrap`** | Native Android C++ / Platform View |
| **Emulator Rendering Surface** | `GLRetroView` (SurfaceView) | Android GLSurfaceView | **`Retain` & `Wrap`** | `AndroidView` Platform View in Flutter |
| **ROM Directory Scanner** | `LocalStorageProvider` / SAF | Android `DocumentFile` | **`Reimplement` + `Wrap`** | Dart `dart:io` + Native SAF MethodChannel |
| **Database & Metadata Storage** | `RetrogradeDatabase` (Room) | SQLite, Room Kapt | **`Replace`** | `drift` SQLite database in Dart |
| **Game Library UI** | Compose Mobile / Leanback TV | Compose, Material, Leanback | **`Simplify` & `Reimplement`** | Responsive Flutter UI (`go_router`) |
| **Save State Controller** | `StatesManager` / `SavesManager` | Android IO, Libretro JNI | **`Reimplement`** | Dart `SaveStatesRepository` |
| **Touch Control Skins** | `lemuroid-touchinput` | Custom Android Views / Canvas | **`Reimplement`** | Flutter `CustomPainter` & Widgets |
| **Gamepad Binding & Remap** | `InputDeviceManager` | Android KeyEvent / MotionEvent | **`Reimplement`** | Flutter `HardwareKeyboard` & Focus |
| **Cheat Downloader & Parser** | `CheatManager` / CwCheatParser | OkHttp, ZipInputStream | **`Reimplement`** | Dart `CheatService` & regex parser |
| **Core Option Variables** | `CoreVariablesManager` | SharedPreferences / FlowPref | **`Reimplement`** | Dart `SharedPreferencesWithCache` |
| **BIOS Hash Validator** | `BiosManager` | Crypto MD5, File IO | **`Reimplement`** | Dart Crypto & File service |
| **Cloud Save Sync** | `SaveSyncManager` | Google Drive API, WorkManager | **`Defer`** | Post-MVP Flutter plugin |
| **Dagger / Kapt / RxJava** | Project-wide | Dagger 2, RxJava 2 | **`Remove`** | Flutter Riverpod state management |
