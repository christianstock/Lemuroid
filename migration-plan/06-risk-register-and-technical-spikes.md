# Risk Register & Technical Spikes

## 1. Prioritized Architectural Risk Register

This document identifies high-severity technical, performance, and memory risks associated with building **GameGrl** for Game Boy (GB, GBC, GBA via mGBA) emulation, RetroAchievements, real-time rewind, and LCD shaders.

| Risk ID | Severity | Likelihood | Risk Description | Primary Impact | Mitigation Strategy |
| :--- | :---: | :---: | :--- | :--- | :--- |
| **RISK-01** | **High** | **Low** | **mGBA Platform View Frame Stutter**: Flutter `AndroidView` composition overhead causing audio crackling in fast GBA games. | Frame drops (< 60 FPS) on low-end devices. | Use Texture Layer Hybrid Composition (TLHC) and optimize mGBA EGL surface buffer swaps. |
| **RISK-02** | **High** | **Medium** | **mGBA Rewind Ring Buffer Memory Pressure**: Allocating 20–50 MB native ring buffer for real-time frame rewind causing low-memory kills. | App process termination during extended rewind sessions. | Implement dynamically capped rewind buffers (e.g. 15s / 30s configurable) with compressed state diffing in C++. |
| **RISK-03** | **Medium** | **Medium** | **LCD/CRT Shader GPU Overhead**: Heavy EGL GLSL display shaders (`crt-easymode.glsl`) dropping frame rate below 60 Hz. | Video stutter during active gameplay. | Provide lightweight fallback GLSL shaders (`lcd-grid.glsl`, bilinear filtering) auto-selected on lower-end GPUs. |
| **RISK-04** | **High** | **Medium** | **RetroAchievements RAM Polling Overhead**: Real-time memory inspection by `rcheevos` C library interfering with mGBA's `retro_run()` loop timing. | Periodic micro-stutters during achievement checks. | Execute `rcheevos` RAM evaluation asynchronously on a separate C++ background thread or during vblank callbacks. |
| **RISK-05** | **Medium** | **High** | **SAF GBA Directory Scan Latency**: Dart-side traversal of Android Storage Access Framework URIs slowing down catalog indexing. | App freeze during initial ROM setup. | Delegate bulk tree traversal to a native C++/Kotlin thread worker over platform channels. |

---

## 2. Technical Proof-of-Concept Spikes

---

### SPIKE-01: Native mGBA Surface Embedding (`GLRetroView` in `AndroidView`)

- **Objective**: Validate that `GLRetroView` renders mGBA (`mgba.so`) frames inside Flutter `AndroidView` at a sustained 60 FPS without EGL deadlocks.
- **Hypothesis**: Texture Layer Hybrid Composition (TLHC) delivers smooth 60 Hz mGBA output with sub-8ms touch latency.
- **Success Criteria**: Sustained 60 FPS video playback with zero EGL crashes on Activity pause/resume.

---

### SPIKE-02: mGBA Real-Time Frame Rewind Buffer Spike

- **Objective**: Test native JNI allocation of 30-second mGBA frame rewind ring buffer and execution of `rewindStep` calls.
- **Hypothesis**: Pushing compressed mGBA state diffs to a 30 MB ring buffer enables 60 FPS rewind without memory leaks.
- **Implementation Steps**:
  1. Allocate native ring buffer in `libretrodroid.cpp` (`retro_serialize_size`).
  2. Capture state snapshot every N frames during `retro_run()`.
  3. Invoke `rewindStep()` from Dart on rewind button press.
- **Success Criteria**:
  - Holding rewind button smoothly reverses gameplay state backward in real-time.
  - Total heap memory allocation remains under 35 MB.

---

### SPIKE-03: RetroAchievements `rcheevos` C Memory Inspection Spike

- **Objective**: Verify that `rcheevos` native C library can poll mGBA RAM addresses and trigger achievement unlocks without impacting frame timing.
- **Hypothesis**: Binding mGBA's `retro_get_memory_data(RETRO_MEMORY_SYSTEM_RAM)` to `rcheevos` evaluation loops adds under 0.5 ms per frame.
- **Success Criteria**: Event channel receives unlock payload with achievement ID, title, and badge URL.

---

### SPIKE-04: EGL LCD & CRT Shader Performance Benchmarking

- **Objective**: Benchmark GPU frame render times across LCD Grid, LCD Dot Matrix, and CRT scanline GLSL shaders.
- **Hypothesis**: Standard mobile GPUs (Adreno / Mali) render LCD grid GLSL shaders at < 4 ms per frame.
- **Success Criteria**: All LCD shader presets sustain 60 FPS frame rate on target handheld devices.
