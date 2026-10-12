# Risk Register & Technical Spikes

## 1. Prioritized Architectural Risk Register

This document identifies high-severity technical, performance, and compatibility risks associated with migrating Lemuroid to Flutter/Dart for Game Boy (GB, GBC, GBA via mGBA) emulation with RetroAchievements integration.

| Risk ID | Severity | Likelihood | Risk Description | Primary Impact | Mitigation Strategy |
| :--- | :---: | :---: | :--- | :--- | :--- |
| **RISK-01** | **High** | **Low** | **mGBA Platform View Frame Stutter**: Flutter `AndroidView` composition overhead causing audio crackling or frame drops in fast GBA games. | Frame drops (< 60 FPS) on low-end devices. | Use Texture Layer Hybrid Composition (TLHC) and optimize mGBA EGL surface buffer swaps. |
| **RISK-02** | **High** | **Medium** | **RetroAchievements RAM Polling Overhead**: Real-time memory inspection by `rcheevos` C library interfering with mGBA's `retro_run()` loop timing. | Periodic micro-stutters during achievement condition checks. | Execute `rcheevos` RAM evaluation asynchronously on a separate C++ background thread or during mGBA vblank callbacks. |
| **RISK-03** | **Medium** | **Medium** | **RetroAchievements API Rate Limiting**: Intermittent network failures or API throttling during user login or badge thumbnail fetching. | Failed achievement unlocks or missing badge images. | Implement local SQLite caching of achievement definitions and badge icons with offline queue retry logic. |
| **RISK-04** | **High** | **High** | **SAF GBA Directory Scan Latency**: Dart-side traversal of Android Storage Access Framework URIs slowing down catalog indexing. | App freeze during initial ROM library setup. | Delegate bulk tree traversal to a native C++/Kotlin thread worker over platform channels. |
| **RISK-05** | **Medium** | **Low** | **mGBA Save State Version Incompatibility**: Changes in mGBA core versions causing legacy `.state` binary files to fail serialization. | Inability to load existing save state slots. | Provide automatic fallback to SRAM (`.srm`) in-game battery saves upon save state load failure. |

---

## 2. Technical Proof-of-Concept Spikes

---

### SPIKE-01: Native mGBA Surface Embedding (`GLRetroView` in `AndroidView`)

- **Objective**: Validate that `GLRetroView` renders mGBA (`mgba.so`) frames inside a Flutter `AndroidView` at a sustained 60 FPS without memory leaks or EGL deadlocks.
- **Hypothesis**: Texture Layer Hybrid Composition (TLHC) delivers smooth 60 Hz mGBA output with sub-8ms touch latency.
- **Implementation Steps**:
  1. Build minimal Flutter app with native Android platform view factory for `GLRetroView`.
  2. Embed `mgba.so` and launch a test Game Boy Advance ROM (e.g., *Super Mario Advance*).
  3. Measure frame rendering rate using Android Profiler over 10 minutes of active gameplay.
- **Success Criteria**: Sustained 60 FPS video playback with zero EGL crashes on Activity pause/resume.

---

### SPIKE-02: RetroAchievements `rcheevos` C Memory Inspection Spike

- **Objective**: Verify that `rcheevos` native C library can poll mGBA RAM addresses and trigger achievement unlocks without impacting frame timing.
- **Hypothesis**: Binding mGBA's `retro_get_memory_data(RETRO_MEMORY_SYSTEM_RAM)` to `rcheevos` evaluation loops adds under 0.5 ms per frame.
- **Implementation Steps**:
  1. Load `rcheevos` C library into JNI bridge.
  2. Parse a sample RetroAchievements trigger string for a GBA game.
  3. Hook trigger evaluation to mGBA frame ticks and emit `onAchievementUnlocked` event channel call when RAM values match.
- **Success Criteria**:
  - Event channel receives unlock payload with achievement ID, title, and badge URL.
  - mGBA frame rendering rate remains at 60 FPS without frame dips.

---

### SPIKE-03: SAF Storage Scanner Engine Spike

- **Objective**: Prove that native KotlinSAF tree reader can scan 500+ GBA/GBC ROM content URIs in under 3 seconds.
- **Hypothesis**: Querying `ContentResolver` in batch returning a single array payload is 10x faster than Dart `DocumentFile` loops.
- **Implementation Steps**:
  1. Write Kotlin SAF tree worker querying content URIs in batch.
  2. Measure execution duration for 500 mock GBA files on Android 11+ SAF directory.
- **Success Criteria**: Complete indexing of 500 ROMs in under 3 seconds with zero UI thread freeze.

---

### SPIKE-04: Database Migration Validation (`RetrogradeDatabase` to Drift)

- **Objective**: Ensure 100% data integrity when opening an existing Room `retrograde.db` database with Dart `drift`.
- **Hypothesis**: Drift SQLite opens and migrates existing Room tables without schema errors.
- **Implementation Steps**:
  1. Seed sample `retrograde.db` with 50 GBA games and favorites.
  2. Open file with Drift `AppDatabase` and execute CRUD operations.
- **Success Criteria**: All 50 records and favorite statuses read and write correctly.
