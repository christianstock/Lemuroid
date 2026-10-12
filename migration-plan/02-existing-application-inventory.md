# Existing Application Inventory & Disposition Analysis

## 1. Overview of Current Repository Architecture & Scope Adjustments

The current repository is an Android Gradle multi-module project. As part of this Flutter clean-room migration, the project scope is focused on a **handheld-first Game Boy ecosystem (GB, GBC, GBA via mGBA)** with **RetroAchievements support**, while **completely removing Android TV**:

- **`lemuroid-app`**:
  - *Mobile Jetpack Compose UI*: Reimplemented in Flutter (`Reimplement`).
  - *Android TV Leanback UI*: Explicitly deleted (`Remove`).
- **`retrograde-app-shared`**: Core library for database entities (`Game`, `GameCheatEntity`), storage scanning, and mGBA core loading (`Reimplement` in Dart + Drift).
- **`lemuroid-touchinput`**: On-screen touch controller layouts for GB, GBC, and GBA (`Reimplement` in Flutter `CustomPainter`).
- **`lemuroid-metadata-libretro-db`**: Libretro binary database parser for GB/GBC/GBA ROM identification (`Reimplement` in Dart).
- **RetroAchievements Integration**: Newly introduced subsystem (`New / Reimplement`).

---

## 2. Functional Inventory & Migration Dispositions

Each subsystem is tagged with one of seven dispositions:
1. **`Retain`**: Kept in native C/C++ (e.g., `mgba.so`, `rcheevos` C library, JNI bridge).
2. **`Wrap`**: Encapsulated behind a clean Flutter Platform Channel API (`GLRetroView`).
3. **`Reimplement`**: Rebuilt natively in Dart/Flutter.
4. **`Replace`**: Replaced with an idiomatic Flutter package (e.g., Room -> Drift SQLite).
5. **`Simplify`**: Streamlined for mobile and handheld screens.
6. **`Defer`**: Postponed for future roadmap expansion (Game & Watch, NDS/3DS).
7. **`Remove`**: Deliberately eliminated (Android TV codebase, non-mGBA core binaries).

---

### Inventory Breakdown

#### 2.1 Emulation Core Engine (`mgba`) & Rendering Surface
- **Purpose**: Low-level mGBA core initialization, EGL frame buffer rendering, PCM audio output stream, GB/GBC/GBA save RAM sync.
- **Existing Files**:
  - `LibretroDroid` library (`com.swordfish.libretrodroid.GLRetroView`, `LibretroDroid`)
  - `lemuroid-cores/lemuroid_core_mgba` (`mgba.so`)
  - `GameViewModelRetroGameView.kt`
- **Disposition**: **`Retain`** (mGBA `mgba.so` C core & LibretroDroid C++ engine) & **`Wrap`** (Exposed to Flutter via `AndroidView` platform view).

---

#### 2.2 RetroAchievements Subsystem
- **Purpose**: RetroAchievements.org user authentication, hardcode/softcore mode enforcement, in-memory achievement trigger evaluation via `rcheevos`, unlock popups, badge caching, and user scoreboards.
- **New Files / Target Path**:
  - `lib/features/retro_achievements/`
  - `android/app/src/main/cpp/rcheevos/` (Native `rcheevos` C library integration)
- **Dependencies**: `rcheevos` C library, RetroAchievements Web API, HTTP client, Flutter Toast/Banner overlay.
- **Disposition**: **`New / Reimplement`** (Native C memory observer + Dart RetroAchievements API repository).

---

#### 2.3 Non-Target Emulation Cores
- **Systems**: PSX, N64, PSP, Arcade/FBNeo, NES/FCEUmm, SNES/Snes9x, Atari, PCE, WonderSwan, NeoGeo.
- **Existing Directories**: `lemuroid-cores/lemuroid_core_pcsx_rearmed`, `lemuroid_core_mupen64plus_next_gles3`, `lemuroid_core_snes9x`, `lemuroid_core_ppsspp`, etc.
- **Disposition**: **`Remove`** (Excluded to minimize APK size and eliminate unused core bridge logic).

---

#### 2.4 Future Roadmap Cores (Game & Watch, NDS, 3DS)
- **Systems**: Game & Watch (`gw`), Nintendo DS (`melonds`), Nintendo 3DS (`citra`).
- **Disposition**: **`Defer`** (Reserved for secondary migration phases after Phase 5 release).

---

#### 2.5 Android TV Codebase & Leanback UI
- **Existing Files**:
  - `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/tv/*` (`MainTVActivity.kt`, `TVGameActivity.kt`, `TVSearchFragment.kt`, `TVFavoritesFragment.kt`, `TVSettingsActivity.kt`, `ChannelHandler.kt`, `TVFolderPickerActivity.kt`)
  - Android TV Leanback dependencies in `build.gradle.kts`.
- **Disposition**: **`Remove`** (Completely deleted. Android TV support is no longer required).

---

#### 2.6 ROM Discovery & Storage Engine (GB / GBC / GBA)
- **Purpose**: Storage Access Framework (SAF) and directory scanning for `.gb`, `.gbc`, `.gba`, and `.zip` files; header serial extraction; magic number verification.
- **Existing Files**: `LocalStorageProvider.kt`, `SerialScanner.kt`, `MagicNumber.kt`, `LemuroidLibrary.kt`.
- **Disposition**: **`Reimplement` + `Wrap`** (Dart file scanner with native SAF content URI batch reader).

---

#### 2.7 Database Persistence & Metadata Storage
- **Purpose**: Local database storage for scanned GB/GBC/GBA ROMs, cover URLs, favorite statuses, and cheat lists.
- **Existing Files**: `RetrogradeDatabase.kt`, `GameDao.kt`, `GameCheatDao.kt`, `DataFileDao.kt`.
- **Disposition**: **`Replace`** (Room replaced by `drift` in Dart, preserving exact SQLite schema for `retrograde.db`).

---

#### 2.8 Save State & Save File Management
- **Purpose**: Auto-save on pause/backgrounding, manual save state slots (slots 0–9) for mGBA, screenshot preview captures, and SRAM (`.srm`) file sync.
- **Existing Files**: `SavesManager.kt`, `StatesManager.kt`, `StatesPreviewManager.kt`.
- **Disposition**: **`Reimplement`** (Dart save slot manager and screenshot store).

---

#### 2.9 Touch Controls & Skins (GB / GBC / GBA)
- **Purpose**: Virtual touch controls and custom skins specifically designed for Game Boy, Game Boy Color, and Game Boy Advance layouts.
- **Existing Files**: `lemuroid-touchinput/*`, `GameBoyArt.kt`, `GameBoyColorArt.kt`, `GameBoyAdvanceArt.kt`.
- **Disposition**: **`Reimplement`** (Rebuilt using Flutter `CustomPainter` and touch gesture detectors).

---

#### 2.10 Cheat Engine & CwCheat Parser
- **Purpose**: Parsing `.cht` and GameShark/ActionReplay cheat strings for GB, GBC, and GBA games; live cheat toggling via `mgba.so`.
- **Existing Files**: `CheatManager.kt`, `CwCheatParser.kt`.
- **Disposition**: **`Reimplement`** (Dart cheat parser and JNI platform channel calls).

---

## 3. Comprehensive Disposition Matrix Summary

| Subsystem / Feature | Current Location | Target Disposition | Target Implementation |
| :--- | :--- | :--- | :--- |
| **mGBA Core Binary (`mgba.so`)** | `lemuroid_core_mgba` | **`Retain`** | Native C `mgba.so` dynamic library |
| **RetroAchievements Engine** | N/A (New feature) | **`New / Reimplement`** | Native `rcheevos` C lib + Dart API |
| **GLRetroView Surface** | `GLRetroView` (SurfaceView) | **`Wrap`** | Flutter `AndroidView` Platform View |
| **Android TV / Leanback UI** | `lemuroid-app/app/tv/` | **`Remove`** | Fully deleted |
| **Non-GBA/GB/GBC Cores** | `lemuroid_core_pcsx`, etc. | **`Remove`** | Excluded from builds |
| **Game & Watch / NDS / 3DS Cores**| `melonds`, `citra`, `gw` | **`Defer`** | Post-MVP expansion roadmap |
| **GB/GBC/GBA Storage Scanner**| `LocalStorageProvider.kt` | **`Reimplement` + `Wrap`**| Dart IO + Native SAF Channel |
| **Database Storage** | `RetrogradeDatabase` (Room)| **`Replace`** | `drift` SQLite database in Dart |
| **Save State Manager** | `SavesManager.kt` | **`Reimplement`** | Dart `SaveStatesRepository` |
| **GB/GBC/GBA Touch Skins** | `lemuroid-touchinput` | **`Reimplement`** | Flutter `CustomPainter` & Widgets |
| **Gamepad Mapping** | `InputDeviceManager.kt` | **`Reimplement`** | Flutter `HardwareKeyboard` & Focus |
| **Dagger / Kapt / RxJava** | Project-wide | **`Remove`** | Flutter Riverpod state management |
