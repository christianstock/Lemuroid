# Executive Overview

## 1. Project Context & Objectives

**Lemuroid** is an open-source Android emulator front-end built on top of [Libretro](https://www.libretro.com/) and [LibretroDroid](https://github.com/Swordfish90/LibretroDroid).

While the original application supported over 20 gaming systems and included complex dual UI codebases for both Android smartphones and Android TV, this **Clean-Room Migration Plan** re-architects the application in **Flutter and Dart** around a **streamlined, handheld-first product vision**:

1. **Focused Core Ecosystem**:
   - Primary support for **Game Boy (GB)**, **Game Boy Color (GBC)**, and **Game Boy Advance (GBA)** powered exclusively by the gold-standard **mGBA** (`mgba`) Libretro core.
   - *Future Expansion Roadmap*: Game & Watch (`gw`) and Nintendo DS / 3DS (`melonds` / `citra`). All other legacy systems (PSX, N64, PSP, Arcade, Atari, PCE, etc.) are explicitly dropped to eliminate code bloat and maintenance overhead.
2. **RetroAchievements Integration**:
   - Native integration with [RetroAchievements.org](https://retroachievements.org/), enabling user login, achievement progress tracking, in-game unlock popups, badge image caching, and Hardcore Mode enforcement via `rcheevos`.
3. **Pure Mobile & Handheld Scope (Android TV Removed)**:
   - **Android TV support is completely removed**. UI engineering efforts focus entirely on touchscreen smartphones, tablets, and dedicated Android handheld gaming devices (e.g., Retroid Pocket, Ayn Odin, Anbernic) with customizable touch controls and physical controller mapping.

---

## 2. Recommended Strategic Direction

### Clean-Room Reimplementation, Not Mechanical Porting
Rather than converting legacy Kotlin/Compose and Leanback Java classes line-by-line, this clean-room strategy:
1. **Adopts a Feature-First Architecture**: Reorganizes the codebase into autonomous feature domains (`emulation_session`, `game_library`, `controller_input`, `save_states`, `retro_achievements`, `game_cheats`, `settings`).
2. **Establishes a Strict Native Retention Boundary**: Retains native C/C++ `mgba` emulator core binaries and `LibretroDroid` JNI bridges. The OpenGL ES frame rendering loop is embedded in Flutter via an `AndroidView` platform view.
3. **Native RetroAchievements Integration**: Connects Libretro core memory addresses to `rcheevos` native C callbacks and async RetroAchievements HTTP APIs, rendering non-intrusive Flutter achievement unlock banners during active gameplay.

```
+-------------------------------------------------------------------------------+
|                               FLUTTER LAYER                                   |
|                                                                               |
|  [Game Library]  [Save States]  [RetroAchievements]  [Touch Skins]  [Settings]|
|        |               |               |                   |             |    |
|        +---------------+---------------+-------------------+-------------+    |
|                                |                                              |
|                    [Emulation Session Controller]                             |
+--------------------------------|----------------------------------------------+
                                 | Platform Channels (Commands/Events)
+--------------------------------v----------------------------------------------+
|                         NATIVE ANDROID LAYER                                  |
|                                                                               |
|  [GLRetroView Platform View (AndroidView)]                                    |
|  [Native JNI Bridge] <---> [mGBA Libretro Core (`mgba.so` C/C++)]            |
|  [rcheevos C Library] <---> [RetroAchievements HTTP API & Memory Polling]     |
|  [Audio Track Stream]       [Native Gamepad & Sensor Listener]                |
+-------------------------------------------------------------------------------+
```

---

## 3. Retained Native Components (`Retain` & `Wrap`)

The following native C/C++ and Java components will be **retained or wrapped**:

1. **mGBA Libretro Core (`mgba.so`)**: Performance-critical C emulator core for GB, GBC, and GBA emulation loaded dynamically via `dlopen`.
2. **`LibretroDroid` Native Engine**: C++ JNI bridge managing `retro_run()`, OpenGL frame buffer drawing, PCM audio queuing, and state serialization.
3. **`rcheevos` C Library Wrapper**: C-library parsing RetroAchievements trigger strings and calculating in-memory achievement unlocks.
4. **`GLRetroView` Platform Surface View**: Android SurfaceView / GLSurfaceView configured with OpenGL ES 2.0/3.0 for zero-copy rendering inside Flutter `AndroidView`.

---

## 4. Reimplemented Flutter/Dart Subsystems (`Reimplement` & `Replace`)

1. **Game Library & Scanner**: Asynchronous document hierarchy scanner, header serial parser (extracting GBA/GBC game IDs), and Libretro DB matching for GB/GBC/GBA titles.
2. **RetroAchievements Flutter Domain**: User login, achievement catalog browsing, progress tracking, unlock notification banners, and badge caching.
3. **Database Persistence (Drift SQLite)**: Replaces Room SQLite with `drift`, keeping exact schema compatibility for `retrograde.db` tables (`games`, `data_files`, `game_cheats`, `achievements`).
4. **Touch Skins & Physical Gamepad Mapping**: Touch overlays for Game Boy, Game Boy Color, and Game Boy Advance with customizable button layout editing.

---

## 5. Explicitly Removed Subsystems (`Remove`)

1. **Android TV Codebase**: Deleted all Leanback Fragment activities, TV search providers, TV folder pickers, and Leanback launcher channel broadcasters.
2. **Non-Target System Cores**: Dropped 17+ non-GBA/GB/GBC core libraries (e.g., PCSX-ReARMed, Mupen64Plus, DeSmuME, Stella, ProSystem, PPSSPP) to streamline binary size and app complexity.

---

## 6. Strategic Benefits

- **Focused Developer Velocity**: Eliminating 17+ core variants and TV code cuts maintenance complexity by over 60%.
- **Delightful RetroAchievements Feature Set**: Direct achievement unlocks and progress badges increase user engagement.
- **Superior Handheld Performance**: Deeply tuned mGBA core integration delivers solid 60 FPS output with sub-8ms input response.
- **Cleaner App Binary Size**: Reduced native `.so` payload significantly shrinks APK footprint.

---

## 7. Critical Risk Summary

1. **`rcheevos` Native Memory Interop**: Ensuring that real-time RAM inspection by `rcheevos` does not lock or slow down the mGBA `retro_run()` loop.
2. **Platform View Frame Latency**: Maintaining 60 FPS output inside Flutter `AndroidView` on lower-end Android devices.
3. **Storage Access Framework Speed**: Fast SAF content URI scanning for large GBA ROM sets.

---

## 8. Phased Roadmap Summary

- **Phase 0: Discovery & Spikes** (GLRetroView embedding, SAF scanning, RetroAchievements `rcheevos` C-library spike).
- **Phase 1: Foundation & Shell** (Flutter architecture, Drift DB, RetroAchievements state, go_router).
- **Phase 2: Emulation Kernel & Touch Skins** (Native mGBA bridge, `AndroidView`, GB/GBC/GBA touch skins).
- **Phase 3: Game Library & Storage Engine** (ROM scanner, serial extractor, cover art loader).
- **Phase 4: Saves, Cheats & RetroAchievements** (Save state slots, cheat engine, live achievement unlock overlay).
- **Phase 5: Polish & Release Candidate** (Physical handheld gamepad mapping, performance benchmarks, release build).
