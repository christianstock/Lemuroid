# Executive Overview

## 1. Project Context & Objectives

**Lemuroid** is an open-source Android emulator front-end built on top of [Libretro](https://www.libretro.com/) and [LibretroDroid](https://github.com/Swordfish90/LibretroDroid). It currently supports over 20 gaming systems (from Atari 2600 to Nintendo 3DS and PSP).

The current Kotlin/Java codebase, while functional, suffers from historical architectural coupling across its Gradle submodules (`lemuroid-app`, `retrograde-app-shared`, `lemuroid-touchinput`, `lemuroid-metadata-libretro-db`, `retrograde-util`). Code is primarily organized by technical layer or legacy package structures rather than feature domain boundaries. Furthermore, managing dual UI systems (Jetpack Compose for phone/tablet and Android Leanback Fragments for Android TV) creates duplication and maintenance overhead.

This document details an implementation-ready **Clean-Room Migration Plan** to rebuild the application in **Flutter and Dart**.

---

## 2. Recommended Strategic Direction

### Clean-Room Reimplementation, Not Mechanical Porting
Rather than converting Kotlin classes into Dart line-by-line, this strategy:
1. **Treats existing source code as functional specification**: Replaces legacy indirection and Dagger/Hilt/Kapt boilerplates with modern, idiomatic Flutter/Dart constructs.
2. **Adopts a Feature-First Architecture**: Reorganizes the codebase into cohesive feature domains (e.g., `emulation_session`, `game_library`, `controller_input`, `save_states`, `game_cheats`, `settings`, `tv_mode`).
3. **Establishes a Strict Native Retention Boundary**: Retains native performance-critical C/C++ emulator cores and the OpenGL ES rendering loop via `LibretroDroid`. The native surface is rendered in Flutter via an `AndroidView` (or `TextureRegistry`) hybrid composition, while high-frequency input and commands flow through low-latency Method/Event Channels.

```
+-----------------------------------------------------------------------+
|                            FLUTTER LAYER                              |
|                                                                       |
|  [Game Library]  [Save States]  [Input Mapping]  [Settings]  [TV UI]    |
|        |               |               |             |          |     |
|        +---------------+-------+-------+-------------+----------+     |
|                                |                                      |
|                    [Emulation Session Controller]                     |
+--------------------------------|--------------------------------------+
                                 | Platform Channels (Commands/Events)
+--------------------------------v--------------------------------------+
|                         NATIVE ANDROID LAYER                          |
|                                                                       |
|  [LibretroDroid Platform View (GLRetroView / AndroidView)]            |
|  [Native JNI Core Loader] <---> [Libretro Core (.so C/C++ Libraries)] |
|  [Audio Track / OpenSL ES]      [Native Sensor / Gamepad Handler]     |
+-----------------------------------------------------------------------+
```

---

## 3. Retained Native Components (`Retain` & `Wrap`)

The following subsystems will **NOT** be rewritten in Dart, as doing so would cause prohibitive performance degradation or necessitate reinventing complex JNI bridge logic:

1. **Libretro Core Binaries (`.so`)**: C/C++ emulator cores (e.g., Snes9x, PCSX-ReARMed, PPSSPP, DeSmuME, Citra) loaded dynamically via `dlopen`.
2. **`LibretroDroid` Native Wrapper**: The JNI C++ layer managing retro_run(), frame callback rendering to OpenGL textures, audio PCM buffer queuing, and save state byte-stream serialization.
3. **Native Android Platform Surface View (`GLRetroView`)**: Android SurfaceView / GLSurfaceView configured with OpenGL ES 2.0/3.0 context for rendering Libretro video frames without copying pixels through Flutter Dart memory.
4. **Hardware Sensor & Gamepad Event Pipeline**: Low-level Android `InputDevice` and `SensorManager` listeners attached to the native activity view to ensure minimum input latency before hitting Libretro's `input_poll` callback.

---

## 4. Reimplemented Flutter/Dart Subsystems (`Reimplement` & `Replace`)

The following subsystems will be rebuilt in Dart:

1. **User Interface & Navigation**: Unified Flutter UI serving both Mobile/Tablet touch devices and Android TV / Gamepad D-pad navigation, using `go_router` and responsive design policies.
2. **Game Library Indexer & Scanner**: Asynchronous document hierarchy scanning (via Storage Access Framework or native directory listing), checksum extraction (CRC32, MD5), and Libretro DB matching.
3. **Database & Metadata Persistence**: Replaced Room SQLite with `drift` (type-safe SQLite for Dart) or `isar`, using exact schema migrations to ensure 100% backward compatibility with existing `retrograde.db` databases.
4. **Touch Input Overlays & Skins**: Flexible Flutter custom painters and touch gesture detectors replacing `lemuroid-touchinput` and XML vector-based controller layouts.
5. **Save State & Cheat Managers**: Dart state controllers managing save state previews, slot selection, and INI `.cht` / CwCheat file parsing.

---

## 5. Strategic Benefits

- **Unified UI Codebase**: Eliminates separate Android TV Leanback XML layout trees in favor of focusable Flutter widgets.
- **Improved Maintainability**: Clear domain isolation prevents cross-module leakage and simplifies unit and widget testing.
- **Enhanced Developer Experience**: Faster iteration with Flutter Hot Reload and concise Dart state management (Riverpod / BLoC).
- **Reduced Binary Overhead**: Replaces heavy AndroidX dependencies, Compose BOM, and Dagger annotation processing with lightweight Dart packages.

---

## 6. Critical Risk Summary

1. **Rendering Performance & Latency**: Flutter `AndroidView` overhead must be minimized using Hybrid Composition or SurfaceTexture integration to maintain 60 FPS / 60 Hz frame pacing.
2. **Storage Access Framework (SAF) Performance**: Document File tree iteration in Android SAF can be slow; Dart must interface with native Kotlin channels for bulk directory operations.
3. **Save File Coherency**: Incorrect path resolution or thread synchronization could corrupt SRAM (`.srm`) or state (`.state`) files during autosave or application destruction.

---

## 7. Phased Roadmap Summary

- **Phase 0: Discovery & Spikes** (Spikes on `GLRetroView` embedding, input latency, SAF scanning).
- **Phase 1: Foundation & Shell** (Flutter project structure, Riverpod setup, Drift database schema, navigation).
- **Phase 2: Core Emulation & Input Boundary** (Native plugin wrapper, `AndroidView` integration, gamepad & touch mapping).
- **Phase 3: Game Library & Storage Engine** (ROM scanner, Libretro DB matching, cover art caching).
- **Phase 4: Saves, Cheats & Session Features** (Save state slots, cheat engine, core variables, rumble/motion).
- **Phase 5: TV Experience & Final Polish** (Gamepad focus navigation, Leanback channels, performance tuning).
