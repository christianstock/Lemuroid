# Executive Overview

## 1. Project Context & Objectives

**GameGrl** is a clean-room Flutter and Dart reimplementation of the Lemuroid emulation engine, transformed into a bespoke, high-performance handheld gaming frontend for Android.

Rather than retaining stock Google Material Design tiles or mechanical package translations, **GameGrl** features a **completely custom, handheld-tailored user experience** designed for retro gaming purists:

```
                  +-----------------------------------+
                  |             GameGrl               |
                  |     Custom Handheld Carousel      |
                  +-----------------+-----------------+
                                    |
          +-------------------------+-------------------------+
          |                                                   |
+---------v----------+                             +----------v---------+
|  Game Info Card    |                             |   Custom Games     |
|  - Front/Back Art  |                             |   - Custom Themes  |
|  - PDF Manuals     |                             |   - Palette Picker |
+---------+----------+                             +----------+---------+
          |                                                   |
          +-------------------------+-------------------------+
                                    |
                  +-----------------v-----------------+
                  |      mGBA Core Gameplay Session   |
                  |  - Real-time Rewind & Fast-Fwd    |
                  |  - Light Sensor (Boktai)          |
                  |  - LCD / CRT Display Shaders      |
                  |  - In-Game Pause Menu             |
                  |  - RetroAchievements (Hardcore)   |
                  +-----------------------------------+
```

---

## 2. Core Pillars of GameGrl

### Pillar 1: Custom-Built Handheld UI (Scrapping Material Design)
- **Home Carousel & Coverflow**: Smooth, custom-animated 3D game carousel replacing standard grid lists.
- **Interactive Game Info Card**: Displays game stats, description, and an interactive **3D Front & Back Box Art Flipper**.
- **Embedded PDF Game Manual Viewer**: Integrated PDF engine (`PdfManualProcessor.kt`) allowing users to read game manuals with page pinch-zoom and bookmarking.
- **GameGrl Themes & Game Boy Palettes**: Custom theme engine supporting OLED dark skins and authentic Game Boy color palette presets (DMG Green, Pocket Gray, Light Blue, Atomic Purple).

### Pillar 2: mGBA Core Focus & Enhanced Emulation Capabilities
- **Primary Systems**: **Game Boy (GB)**, **Game Boy Color (GBC)**, and **Game Boy Advance (GBA)** powered by the **mGBA** (`mgba.so`) Libretro core.
- **Future Expansion Roadmap**: Game & Watch (`gw`), Nintendo DS / 3DS (`melonds` / `citra`), and **mGBA Local Wi-Fi Link Cable Multiplayer** (post-MVP). All other legacy systems are explicitly dropped.
- **Real-Time Frame Rewind**: Up to 30 seconds of real-time state rewind with a single button press or dedicated touch gesture.
- **Fast-Forward Speed Multiplier**: Variable speed accelerator (1.5x, 2x, 4x, 8x) for mGBA emulation with low-latency audio pitch adjustment.
- **Hardware Light Sensor (`RETRO_SENSOR_ILLUMINANCE`)**: Maps ambient light sensor (`Sensor.TYPE_LIGHT`) to Libretro sensor ID 6 for GBA solar games (*Boktai*). Devices lacking a light sensor simply ignore illuminance (players use solar ROM hacks).
- **Microphone Input (`RECORD_AUDIO`)**: Hardware microphone buffer streaming for mic-dependent ROMs.
- **In-Game Pause & Session Recovery**: Pause overlay providing state slot selection, quick-save, quick-load, and game reset.
- **LCD & CRT Display Shaders**: Authentic EGL display shaders (`ShaderChooser.kt`) including LCD Grid, LCD Dot Matrix, Bilinear Filtering, and CRT Scanlines.

### Pillar 3: RetroAchievements Integration
- **RetroAchievements.org Native Engine**: Integrated `rcheevos` C memory observer evaluating RAM conditions in real-time.
- **Hardcore Mode Enforcer**: Hardcore Mode automatically disables Rewind and Save State loading to maintain achievement integrity.
- **Unlock Banners & Badges**: Non-intrusive animated achievement unlock banners over gameplay and a cached badge catalog.

---

## 3. High-Level Architecture Overview

```
+-------------------------------------------------------------------------------+
|                             GAMEGRL FLUTTER LAYER                             |
|                                                                               |
|  [Custom Carousel]  [Game Info Card]  [PDF Manuals]  [3D Front/Back Box Art]  |
|  [RetroAchievements]  [Rewind / Fast-Fwd]  [Game Palettes]  [Custom Themes]     |
|        |                  |                |                  |               |
|        +------------------+----------------+------------------+               |
|                                   |                                           |
|                      [Emulation Session Notifier]                             |
+-----------------------------------|-------------------------------------------+
                                    | Platform Channels (Commands/Events)
+-----------------------------------v-------------------------------------------+
|                            NATIVE ANDROID LAYER                               |
|                                                                               |
|  [GLRetroView Platform View (AndroidView)]                                    |
|  [Native JNI Bridge] <---> [mGBA Libretro Core (`mgba.so` C/C++)]            |
|  [mGBA Rewind Buffer]       [EGL LCD / CRT Shader Pipeline]                   |
|  [rcheevos C Library] <---> [RetroAchievements Memory Polling]                |
|  [Hardware Sensors]  <---> [Accelerometer, Gyroscope & Light Sensor (ID 6)]   |
+-------------------------------------------------------------------------------+
```

---

## 4. Retained vs Reimplemented Subsystems

| Subsystem | Existing Location | Target Disposition | Target Implementation |
| :--- | :--- | :--- | :--- |
| **mGBA Core Binary (`mgba.so`)** | `lemuroid_core_mgba` | **`Retain`** | Native C `mgba.so` library |
| **RetroAchievements Engine** | N/A (New feature) | **`New / Reimplement`**| Native `rcheevos` C + Dart API |
| **Light Sensor (Boktai)** | `MotionManager.kt` | **`Reimplement`** | `Sensor.TYPE_LIGHT` -> ID 6 |
| **Microphone Input** | `GameViewModelRetroGameView.kt` | **`Reimplement`** | `RECORD_AUDIO` audio stream |
| **Real-Time Frame Rewind** | `LibretroDroid.java` | **`Wrap` & `Reimplement`**| mGBA rewind buffer JNI calls |
| **Fast-Forward Speed Toggle** | `GameActivity.kt` | **`Reimplement`** | mGBA speed multiplier toggle |
| **Deep Link Game Launcher** | `DeepLink.kt` | **`Reimplement`** | `gamegrl://launch?gameId=...` |
| **LCD / CRT Display Shaders** | `ShaderChooser.kt` | **`Wrap`** | EGL GLSL shader pipeline |
| **PDF Game Manual Viewer** | `PdfManualProcessor.kt` | **`Reimplement`** | Flutter PDF manual viewer |
| **3D Front/Back Box Art** | `CoverUtils.kt` | **`Reimplement`** | Flutter 3D Box Art Flipper |
| **Automated Cache Cleaner** | `CacheCleanerWork.kt` | **`Reimplement`** | Background cache cleanup worker |
| **Database Storage** | `RetrogradeDatabase.kt` | **`Replace`** | Drift SQLite (`retrograde.db`) |
| **mGBA Wi-Fi Link Cable** | N/A (Future Feature) | **`Defer`** | Local IP Socket Link Cable (Post-MVP) |
