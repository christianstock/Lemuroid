# Lemuroid Advanced Features Implementation Guide

This document outlines the architecture and implementation plan for Rewind, Enhanced UI, and Authentic Handheld Skins.

---

## 1. Rewind System
### Goal: System-dependent, RAM-aware rewind buffer.

### Implementation Details:
1.  **RAM & System Profiler**:
    *   **ActivityManager**: Check `memoryInfo.availMem`.
    *   **System Save Size**:
        *   GB/GBA: ~100KB - 512KB.
        *   PSX/PSP: 1MB - 10MB+.
    *   **Strategy Decision**:
        *   Low RAM: Disable or 5s intervals @ 30s buffer.
        *   Med RAM: 5s intervals @ 1m buffer.
        *   High RAM: 1s intervals @ 1m buffer.
2.  **Rewind Manager**:
    *   Use `CircularBuffer<ByteArray>` sized according to the chosen strategy.
    *   Periodically capture serialized state from `GLRetroView`.
3.  **Visual Feedback**:
    *   Hold "Rewind" in Top Bar to trigger reverse-restore at 5-10 states/sec.

---

## 2. Interactive Top Bar
### Goal: Instant access with Lemuroid/Skin-native aesthetics.

### Implementation Details:
1.  **Glass Theme**:
    *   Do **NOT** use standard Material `TopAppBar`.
    *   Use a custom `Row` with `GlassSurface` background (matching `PadContainer`).
2.  **Layout**:
    *   `[Save] [Load] [Rewind] [Pause] [Menu]`
3.  **Interactions**:
    *   **Save/Load**:
        *   Short Click: Quick Save/Load via `viewModel.saveQuickSave()`.
        *   Long Click: `navController.navigate(GameMenuRoute.SAVE/LOAD)`.
    *   **Pause**: Call `retroGameView.pause()` / `resume()`.

---

## 3. Visual Save/Load Menu
### Goal: Screenshots and timestamps for states.

### Implementation Details:
1.  **Screenshot Capture**:
    *   `BaseGameActivity` uses `GLRetroView.captureBitmap()` during save.
    *   Save to `DirectoriesManager.getStatesPreviewDirectory()` as `$gameId_$slot.png`.
2.  **UI**:
    *   `LazyVerticalGrid` displaying `Card` with:
        *   `Image` (Coil).
        *   Date/Time of the file.

---

## 4. Authentic Handheld Skins (GB, GBC, GBA)
### Goal: Vector-accurate, DPI-aware skins with persistence.

### Implementation Details:
1.  **Selection Persistence**:
    *   Store `lastSelectedSkin` in `SharedPreferences`.
    *   Default: "Lemuroid" (original glass theme).
2.  **DPI-Aware Scaling**:
    *   `val dotsPerMm = density.density * 160f / 25.4f`.
    *   Target Screen Sizes (mm):
        *   GB/GBC: 47mm x 43mm.
        *   GBA: 61mm x 41mm.
3.  **Layouts**:
    *   Portrait: Authentic handheld look (controls below screen).
    *   Landscape: Authentic "Wide" look (controls on sides of screen).
    *   GB games on GBA skin should use the GBA landscape form factor (D-pad left, buttons right).

---

## 5. Implementation Caveats
*   **DPI Errors**: Provide "Manual Scale" calibration in settings.
*   **Performance**: PSX/PSP state serialization is heavy; ensure profiling prevents OOM.
