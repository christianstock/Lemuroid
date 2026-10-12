# Existing Application Inventory & Disposition Analysis

## 1. Overview of Repository Architecture & GameGrl Scope

The existing Android Gradle repository is being reimplemented into **GameGrl**—a custom-designed Flutter handheld emulator.

All standard Android Material Design layouts, Compose BOM dependencies, and Android TV Leanback components are removed and replaced with custom-built GameGrl handheld widgets:

- **`lemuroid-app`**:
  - *Jetpack Compose / Material UI*: Replaced with custom GameGrl Handheld Carousel, Game Cards, and Games View (`Reimplement`).
  - *Android TV Leanback UI*: Explicitly deleted (`Remove`).
- **`retrograde-app-shared`**:
  - *mGBA Core & JNI Engine*: Retained (`Retain` & `Wrap`).
  - *PDF Manual Processor*: Rebuilt as Flutter PDF manual viewer (`Reimplement`).
  - *Shader Chooser*: EGL GLSL LCD and CRT display shaders (`Wrap`).
  - *Cover Utils*: Expanded to dual-sided Front & Back 3D Box Art (`Reimplement`).
  - *Rewind Buffer*: JNI rewind frame buffer bridge (`Wrap` & `Reimplement`).

---

## 2. Functional Inventory & Migration Dispositions

### 2.1 Emulation Core (`mgba`), Rewind & Shaders
- **mGBA Core Engine (`mgba.so`)**: **`Retain`** (C core binary for GB, GBC, GBA).
- **GLRetroView Surface View**: **`Wrap`** (Flutter `AndroidView` platform view).
- **Real-Time Frame Rewind**:
  - *Existing Files*: `LibretroDroid.java` (`setRewindBuffer`, `rewindStep`).
  - *Disposition*: **`Wrap` & `Reimplement`** (Dart `RewindController` invoking JNI frame rewind steps).
- **LCD & CRT Display Shaders**:
  - *Existing Files*: `ShaderChooser.kt` (`lcd-grid.glsl`, `crt-easymode.glsl`).
  - *Disposition*: **`Wrap`** (EGL shader selection via platform channels).

---

### 2.2 GameGrl Custom Handheld UI Subsystems
- **Custom Home Carousel & Coverflow**:
  - *Disposition*: **`New / Reimplement`** (Custom 3D animated game carousel replacing Material lists).
- **3D Front & Back Box Art Flipper**:
  - *Existing Files*: `CoverUtils.kt`.
  - *Disposition*: **`Reimplement`** (Dual-sided 3D transform card flipper rendering front cover and back box art scans).
- **PDF Game Manual Viewer**:
  - *Existing Files*: `PdfManualProcessor.kt`, `GameManualViewerOverlay.kt`, `ZoomableManualPage.kt`.
  - *Disposition*: **`Reimplement`** (Embedded Flutter PDF viewer with page thumbnails and pinch-zoom).
- **GameGrl Theme & Game Boy Palette Engine**:
  - *Existing Files*: `GameBoyArt.kt`, `GameBoyColorArt.kt`, `PspArt.kt`.
  - *Disposition*: **`Reimplement`** (Custom theme engine providing GameGrl color schemes and DMG Green / Pocket Gray LCD palette swappers).
- **In-Game Pause & Session Recovery Menu**:
  - *Existing Files*: `GameMenuHelper.kt`, `GameMenuContract.kt`.
  - *Disposition*: **`Reimplement`** (Custom pause menu with state slot quick-save, quick-load, and game reset).

---

### 2.3 RetroAchievements Subsystem
- **Purpose**: User login, hardcode mode, in-memory achievement trigger evaluation via `rcheevos` C library, unlock popups, and badge image caching.
- **Disposition**: **`New / Reimplement`** (Native C memory observer + Dart RetroAchievements repository).

---

### 2.4 Android TV & Legacy System Removal
- **Android TV Components**: `MainTVActivity.kt`, `TVGameActivity.kt`, `TVSearchFragment.kt`, `ChannelHandler.kt`, `TVFolderPickerActivity.kt`. -> **`Remove`**.
- **Non-Target System Cores**: PCSX-ReARMed, Mupen64Plus, Snes9x, PPSSPP, Stella, FBNeo, etc. -> **`Remove`**.
- **Future Roadmap Cores**: Game & Watch (`gw`), Nintendo DS (`melonds`), Nintendo 3DS (`citra`). -> **`Defer`**.

---

## 3. Comprehensive Disposition Matrix Summary

| Subsystem / Feature | Existing Location | Target Disposition | Target Implementation |
| :--- | :--- | :--- | :--- |
| **mGBA Core Binary (`mgba.so`)** | `lemuroid_core_mgba` | **`Retain`** | Native C `mgba.so` library |
| **Real-Time Frame Rewind** | `LibretroDroid.java` | **`Wrap` & `Reimplement`**| Dart `RewindController` + JNI |
| **LCD & CRT Display Shaders** | `ShaderChooser.kt` | **`Wrap`** | EGL GLSL shader pipeline |
| **PDF Game Manual Viewer** | `PdfManualProcessor.kt` | **`Reimplement`** | Flutter PDF manual viewer |
| **3D Front/Back Box Art** | `CoverUtils.kt` | **`Reimplement`** | Flutter 3D Box Art Flipper |
| **Custom Handheld Carousel** | N/A (New UI) | **`Reimplement`** | GameGrl Custom Carousel |
| **Game Boy Theme Palettes** | `GameBoyArt.kt` | **`Reimplement`** | GameGrl Palette Engine |
| **In-Game Pause Menu** | `GameMenuHelper.kt` | **`Reimplement`** | GameGrl Custom Pause Menu |
| **RetroAchievements Engine** | N/A (New Feature) | **`New / Reimplement`**| Native `rcheevos` C + Dart API |
| **Database Storage** | `RetrogradeDatabase` (Room)| **`Replace`** | `drift` SQLite database (`retrograde.db`) |
| **Android TV Codebase** | `app/tv/*` | **`Remove`** | Completely deleted |
| **Material Design UI** | Compose Material3 | **`Remove`** | Replaced with custom GameGrl UI |
