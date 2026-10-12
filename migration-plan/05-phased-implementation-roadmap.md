# Phased Implementation Roadmap

## 1. Roadmap Overview & Milestones

The migration is organized into **six incremental phases (Phase 0 through Phase 5)** focused on building **GameGrl**—a custom handheld Game Boy (GB, GBC, GBA via mGBA) emulator with RetroAchievements, real-time rewind, LCD/CRT shaders, PDF game manuals, 3D front/back box art, and custom UI components.

```
+-------------------------------------------------------------------------------+
| Phase 0: Discovery & Technical Spikes                                         |
|  - PoC: GLRetroView inside AndroidView                                        |
|  - PoC: rcheevos Memory Observer & mGBA Frame Rewind Buffer Spike             |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 1: GameGrl Foundation & Theme Engine                                    |
|  - Custom GameGrl Styling & Game Boy LCD Palette Switcher                      |
|  - Drift Database Schema (Games, Front/Back Covers, Manuals, Achievements)    |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 2: mGBA Kernel, Rewind & LCD/CRT Shaders                                |
|  - mGBA Platform View + 30-Second Frame Rewind Controller                     |
|  - EGL LCD Grid / Dot Matrix / CRT Shader Pipeline                            |
|  * RUNNABLE MILESTONE: Launch GBA game with active Rewind & LCD shaders       |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 3: GameGrl Custom Handheld UI, Carousel & Manuals                       |
|  - Custom 3D Coverflow Game Carousel & Games View                             |
|  - 3D Front & Back Box Art Flipper + PDF Game Manual Viewer                   |
|  * RUNNABLE MILESTONE: Full custom handheld UI catalog with manuals & box art |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 4: RetroAchievements, In-Game Pause Menu & Cheats                       |
|  - RetroAchievements Auth, Game Hash & Unlock Toast Banners                   |
|  - In-Game Pause Overlay (State Slots, Quick-Save, Quick-Load, Reset)          |
|  - GameShark / ActionReplay Cheat Engine                                      |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 5: Handheld Controls, Benchmarking & Release Candidate                  |
|  - Physical Handheld Gamepad Mapping & Touch Layout Customizer                |
|  - Performance Benchmarking (Sustained 60 FPS mGBA execution)                 |
|  * RELEASE CANDIDATE MILESTONE: Production-ready GameGrl APK                  |
+-------------------------------------------------------------------------------+
```

---

## 2. Granular Phase Breakdown

### Phase 0: Discovery & Risk Reduction (Technical Spikes)

#### Work Item 0.1: GLRetroView mGBA AndroidView Embedding Spike
- **Objective**: Prove that `GLRetroView` renders mGBA (`mgba.so`) frames inside Flutter `AndroidView` at 60 FPS without memory leaks.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Test GBA ROM runs continuously at 60 FPS in Flutter `AndroidView`.

#### Work Item 0.2: RetroAchievements `rcheevos` C Memory Spike
- **Objective**: Verify that `rcheevos` C library can inspect mGBA RAM addresses without degrading frame pacing.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Native callback triggers event channel call when RAM value changes.

#### Work Item 0.3: mGBA Real-Time Frame Rewind Buffer Spike
- **Objective**: Test native JNI allocation of 30-second mGBA frame rewind buffer and execution of `rewindStep` calls.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Holding rewind button smoothly reverses mGBA gameplay state backward in real-time.

---

### Phase 1: GameGrl Foundation & Theme Engine

#### Work Item 1.1: GameGrl Flutter Repository & Custom Theme Engine
- **Objective**: Establish Flutter project structure, Riverpod graph, custom GameGrl styling, and Game Boy LCD palette swapper (DMG Green, Pocket Gray, Light Blue).
- **Effort Estimate**: **S** (Confidence: High)
- **Acceptance Criteria**: Custom GameGrl theme and palette swapper function cleanly.

#### Work Item 1.2: Drift Database Schema & Room Migration
- **Objective**: Implement Drift SQLite schema mirroring legacy Room `RetrogradeDatabase` tables (`games`, `data_files`, `game_cheats`, `achievements`), adding `backCoverUrl` and `manualUrl`.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Database initializes; legacy `retrograde.db` opens without data loss.

---

### Phase 2: mGBA Kernel, Rewind & LCD/CRT Shaders

#### Work Item 2.1: Native mGBA JNI & Rewind Controller Bridge
- **Objective**: Build JNI bridge for mGBA session lifecycle, core options, and 30-second real-time frame rewind commands.
- **Effort Estimate**: **L** (Confidence: High)
- **Acceptance Criteria**: `initializeCoreAndGame` and `rewindStep` execute smoothly.

#### Work Item 2.2: EGL LCD & CRT Display Shader Pipeline
- **Objective**: Wire EGL GLSL display shaders (`lcd-grid.glsl`, `lcd-dot-matrix.glsl`, `crt-easymode.glsl`) to mGBA render surface.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Selecting LCD or CRT shader preset applies authentic visual filtering instantly.

---

### Phase 3: GameGrl Custom Handheld UI, Carousel & Manuals

#### Work Item 3.1: Custom 3D Coverflow Carousel & Games View
- **Objective**: Build bespoke, non-Material 3D game carousel and custom Games View layout.
- **Effort Estimate**: **L** (Confidence: High)
- **Acceptance Criteria**: Smooth carousel scrolling with cover artwork cards and game stats.

#### Work Item 3.2: 3D Front & Back Box Art Flipper Widget
- **Objective**: Interactive 3D transform card allowing users to tap or swipe to flip between front cover art and back box art scans.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Smooth 3D flip animation showing front and back box artwork.

#### Work Item 3.3: Embedded PDF Game Manual Viewer
- **Objective**: Build zoomable PDF game manual viewer with page thumbnails and pinch-zoom gestures.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Opens PDF manual files smoothly with zoom and page navigation.

---

### Phase 4: RetroAchievements, In-Game Pause Menu & Cheats

#### Work Item 4.1: RetroAchievements Auth & Unlock Banner Overlay
- **Objective**: Authenticate RetroAchievements user credentials, compute MD5 game hash, and render animated unlock toast banners over mGBA gameplay.
- **Effort Estimate**: **L** (Confidence: High)
- **Acceptance Criteria**: User logs in; in-game achievement unlocks trigger animated toast banner.

#### Work Item 4.2: In-Game Pause & Session Recovery Menu
- **Objective**: Custom handheld pause overlay providing quick-save, quick-load, 10 save state slot pickers, and game reset.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Pausing game freezes mGBA session and allows state slot operations.

#### Work Item 4.3: GameShark / ActionReplay Cheat Engine
- **Objective**: Parse `.cht` files and GameShark codes, allowing live toggling via mGBA core.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Toggling cheat updates mGBA memory without crashing.

---

### Phase 5: Handheld Controls, Benchmarking & Release Candidate

#### Work Item 5.1: Physical Gamepad Mapping & Touch Layout Customizer
- **Objective**: Bind physical handheld controller buttons (Retroid/Odin) and build drag-and-drop touch control layout editor.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Physical buttons control gameplay; customized touch button positions persist.

#### Work Item 5.2: Final Benchmarking & Production Build
- **Objective**: Execute automated test suite, verify sustained 60 FPS output for GBA games with LCD shaders, and produce release APK.
- **Effort Estimate**: **S** (Confidence: High)
- **Acceptance Criteria**: 60 FPS performance confirmed; signed release APK generated.

---

## 3. Relative Effort Summary Matrix

| Phase | Work Item | Focus Area | Effort | Confidence |
| :--- | :--- | :--- | :---: | :---: |
| **Phase 0** | 0.1 | GLRetroView mGBA AndroidView Embedding Spike | **M** | High |
| **Phase 0** | 0.2 | RetroAchievements `rcheevos` C Memory Spike | **M** | High |
| **Phase 0** | 0.3 | mGBA Real-Time Frame Rewind Buffer Spike | **M** | High |
| **Phase 1** | 1.1 | GameGrl Custom Theme & Palette Engine | **S** | High |
| **Phase 1** | 1.2 | Drift Database Schema & Room Migration | **M** | High |
| **Phase 2** | 2.1 | Native mGBA JNI & Rewind Controller Bridge | **L** | High |
| **Phase 2** | 2.2 | EGL LCD & CRT Display Shader Pipeline | **M** | High |
| **Phase 3** | 3.1 | Custom 3D Coverflow Game Carousel View | **L** | High |
| **Phase 3** | 3.2 | 3D Front & Back Box Art Flipper Widget | **M** | High |
| **Phase 3** | 3.3 | Embedded PDF Game Manual Viewer Screen | **M** | High |
| **Phase 4** | 4.1 | RetroAchievements Auth & Unlock Banner | **L** | High |
| **Phase 4** | 4.2 | In-Game Pause Menu & State Slot Recovery | **M** | High |
| **Phase 4** | 4.3 | GameShark / ActionReplay Cheat Engine | **M** | High |
| **Phase 5** | 5.1 | Physical Gamepad Mapping & Touch Editor | **M** | High |
| **Phase 5** | 5.2 | Performance Benchmarking & Release Build | **S** | High |
