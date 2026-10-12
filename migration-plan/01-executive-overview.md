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
                  |  - Real-time Rewind (30s buffer)  |
                  |  - LCD / CRT Shaders              |
                  |  - In-Game Pause Menu             |
                  |  - RetroAchievements Overlays     |
                  +-----------------------------------+
```

---

## 2. Core Pillars of GameGrl

### Pillar 1: Custom-Built Handheld UI (Scrapping Material Design)
- **Home Carousel & Coverflow**: Smooth, custom-animated 3D game carousel replacing standard grid lists.
- **Interactive Game Info Card**: Displays game stats, description, and an interactive **3D Front & Back Box Art Flipper**.
- **Embedded PDF Game Manual Viewer**: Integrated PDF engine (`PdfManualProcessor.kt`) allowing users to read game manuals with page pinch-zoom and bookmarking.
- **GameGrl Themes & Game Boy Palettes**: Custom theme engine supporting OLED dark skins and authentic Game Boy color palette presets (DMG Green, Pocket Gray, Light Blue, Atomic Purple, Game Boy Color colorization).

### Pillar 2: mGBA Core Focus & Enhanced Emulation Capabilities
- **Primary Systems**: **Game Boy (GB)**, **Game Boy Color (GBC)**, and **Game Boy Advance (GBA)** powered by the **mGBA** (`mgba.so`) Libretro core.
- **Future Expansion Roadmap**: Game & Watch (`gw`) and Nintendo DS / 3DS (`melonds` / `citra`). All other legacy systems (PSX, N64, PSP, Arcade, Atari) are dropped.
- **Real-Time Frame Rewind**: Up to 30 seconds of real-time state rewind with a single button press or dedicated touch gesture.
- **In-Game Pause & Session Recovery**: Pause overlay providing state slot selection, quick-save, quick-load, and game reset.
- **LCD & CRT Display Shaders**: Authentic EGL display shaders (`ShaderChooser.kt`) including LCD Grid, LCD Dot Matrix, Bilinear Filtering, and CRT Scanlines.

### Pillar 3: RetroAchievements Integration
- **RetroAchievements.org Native Engine**: Integrated `rcheevos` C memory observer evaluating RAM conditions in real-time.
- **Unlock Banners & Badges**: Non-intrusive animated achievement unlock banners over gameplay and a cached badge catalog.

---

## 3. High-Level Architecture Overview

```
+-------------------------------------------------------------------------------+
|                             GAMEGRL FLUTTER LAYER                             |
|                                                                               |
|  [Custom Carousel]  [Game Info Card]  [PDF Manuals]  [3D Front/Back Box Art]  |
|  [RetroAchievements]  [Rewind Controller]  [LCD Shaders]  [Custom Themes]     |
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
+-------------------------------------------------------------------------------+
```

---

## 4. Retained vs Reimplemented Subsystems

| Subsystem | Existing Location | Target Disposition | Target Implementation |
| :--- | :--- | :--- | :--- |
| **mGBA Core Binary (`mgba.so`)** | `lemuroid_core_mgba` | **`Retain`** | Native C `mgba.so` library |
| **RetroAchievements Engine** | N/A (New feature) | **`New / Reimplement`**| Native `rcheevos` C + Dart API |
| **Home Carousel & Games View** | N/A (New UI) | **`Reimplement`** | Custom Flutter Carousel & Cards |
| **3D Box Art Flipper (Front/Back)**| `CoverUtils.kt` | **`Reimplement`** | Flutter 3D Transform Flipper Widget |
| **PDF Game Manual Viewer** | `PdfManualProcessor.kt` | **`Reimplement`** | Flutter PDF render engine |
| **Real-Time Frame Rewind** | `LibretroDroid.java` | **`Wrap` & `Reimplement`**| mGBA rewind buffer JNI calls |
| **LCD / CRT Display Shaders** | `ShaderChooser.kt` | **`Wrap`** | EGL GLSL shader pipeline |
| **Game Boy Theme Palettes** | `GameBoyArt.kt` | **`Reimplement`** | GameGrl Custom Palette Engine |
| **Database Storage** | `RetrogradeDatabase.kt` | **`Replace`** | Drift SQLite (`retrograde.db`) |
| **Android TV Codebase** | `app/tv/*` | **`Remove`** | Completely deleted |
