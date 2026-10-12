# Risk Register & Technical Spikes

## 1. Prioritized Architectural Risk Register

This document identifies high-severity technical, performance, and compatibility risks associated with migrating Lemuroid to Flutter/Dart, along with concrete mitigations and validation spikes.

| Risk ID | Severity | Likelihood | Risk Description | Primary Impact | Mitigation Strategy |
| :--- | :---: | :---: | :--- | :--- | :--- |
| **RISK-01** | **High** | **Medium** | **Platform View Rendering Latency**: Flutter `AndroidView` overhead causing dropped frames or stuttering in fast Libretro cores (N64, PSX, PSP). | Video frame drops (< 60 FPS), audio crackling. | Use Texture Layer Hybrid Composition (TLHC) or fallback to Native Activity/SurfaceView overlay for heavy 3D cores. |
| **RISK-02** | **High** | **High** | **SAF Directory Scan Speed**: Dart-side traversal of Android Storage Access Framework URIs being too slow for large ROM folders (1000+ games). | App freeze during initial library indexing. | Delegate bulk tree traversal and file descriptor queries to a native C++/Kotlin thread worker via MethodChannel. |
| **RISK-03** | **Medium** | **Medium** | **Input Processing Latency**: Touch gesture recognition delay in Flutter overlay controls affecting fast-paced platformer/arcade games. | Perceived controller lag by end users. | Route high-frequency touch events directly to native C++ JNI input buffers, bypassing main Dart event loop queues. |
| **RISK-04** | **High** | **Low** | **Save State Corruption**: Discrepancies in file pathing or async write timing causing corrupted `.state` files during auto-save or app termination. | Permanent loss of user game progress. | Implement atomic file write patterns (`.tmp` write then swap) and strict byte-array checksum verification before overwriting. |
| **RISK-05** | **Medium** | **Medium** | **Android TV D-Pad Focus Lock**: Flutter `FocusNode` graph failing to navigate complex custom grids on Android TV devices using gamepads. | Broken navigation on TV screens. | Wrap all TV interactive elements in explicit directional focus policies (`FocusTraversalGroup`) with TV UI testing automation. |

---

## 2. Technical Proof-of-Concept Spikes

To eliminate architectural uncertainties before commencing full implementation, four targeted technical spikes are defined below.

---

### SPIKE-01: Native OpenGL Surface Embedding (`GLRetroView` in `AndroidView`)

- **Objective**: Validate that `GLRetroView` renders Libretro EGL frames within a Flutter `AndroidView` at 60 FPS without memory leaks or GL context loss on Activity lifecycle changes.
- **Hypothesis**: Flutter TLHC (Texture Layer Hybrid Composition) provides sufficient performance for 60 Hz Libretro core rendering without frame buffer copy overhead.
- **Implementation Steps**:
  1. Construct a lightweight Flutter application with a native Android `PlatformViewFactory`.
  2. Instantiate `GLRetroView` inside the factory and load a test Game Boy Advance ROM using the `mgba` dynamic library (`.so`).
  3. Measure frame rendering rate using Android Systrace / Profiler during active gameplay.
  4. Trigger screen rotation and Activity pause/resume cycles to test EGL surface recreation.
- **Success Criteria**:
  - Stable 60 FPS playback on target test devices (e.g., Pixel / Android TV box).
  - Clean resume after backgrounding without GL context crashes or black screens.
- **Go / No-Go Gate**: If frame rate falls below 55 FPS or EGL context deadlocks, adopt a dual-activity model where emulation runs in a dedicated native `SurfaceView` Activity while Flutter handles all front-end UI.

---

### SPIKE-02: High-Performance Storage Access Framework (SAF) Engine

- **Objective**: Prove that bulk ROM folder indexing over SAF can process over 100 files per second.
- **Hypothesis**: Dart `MethodChannel` invoking native Android `ContentResolver.query()` in a background thread is 10x faster than Dart-side `DocumentFile` traversal.
- **Implementation Steps**:
  1. Write Kotlin SAF tree worker querying content URIs in batch using `DocumentsContract.buildChildDocumentsUriUsingTree()`.
  2. Pass structured list of file names, sizes, and content URIs to Dart via single array payload.
  3. Measure index duration for 1,000 mock files on Android 11+ SAF directory.
- **Success Criteria**:
  - Complete scanning of 1,000 ROM URIs in under 5 seconds.
  - Zero UI thread jank during scanning.
- **Go / No-Go Gate**: If batch query exceeds 10 seconds, implement native NDK file reader using `libstree` / native C SAF bindings.

---

### SPIKE-03: Ultra-Low-Latency Touch & Gamepad Input Dispatch

- **Objective**: Verify that Flutter touch control gestures and physical gamepad inputs add less than 8 ms of input latency to Libretro's `input_poll`.
- **Hypothesis**: Direct JNI input state bitmask updates bypassing Flutter's standard event channel queue eliminate input lag.
- **Implementation Steps**:
  1. Build a custom Flutter touch overlay widget that computes button state changes inside `Listener.onPointerDown` / `onPointerMove`.
  2. Compare standard `MethodChannel` input dispatch vs direct C++ shared memory bitmask updates.
  3. Measure input-to-frame response time using high-speed video capture (240 FPS).
- **Success Criteria**:
  - Total input dispatch latency under 8 ms (less than 1 frame at 60 Hz).
  - No dropped inputs during multi-touch button combinations (e.g., diagonal D-pad + A + B).

---

### SPIKE-04: Database Migration Validation (`RetrogradeDatabase` to Drift)

- **Objective**: Ensure 100% data integrity when opening an existing Room `retrograde.db` SQLite database with Dart `drift`.
- **Hypothesis**: Drift can open and migrate existing Room SQLite database files without breaking primary key indices or table schemas.
- **Implementation Steps**:
  1. Populate a sample `retrograde.db` using the current Kotlin app with 50 games, cover URLs, favorites, and cheat entries.
  2. Copy the DB file into the Flutter test app directory.
  3. Open the file with Drift and perform read/write queries on `games`, `data_files`, and `game_cheats` tables.
- **Success Criteria**:
  - All 50 games and associated metadata read correctly without schema errors.
  - New insertions and favorite status toggles persist seamlessly.
