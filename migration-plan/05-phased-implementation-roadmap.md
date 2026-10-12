# Phased Implementation Roadmap

## 1. Roadmap Overview & Milestones

The migration is organized into **six incremental phases (Phase 0 through Phase 5)** focused on a handheld-first Game Boy (GB, GBC, GBA via mGBA) emulator with RetroAchievements support.

```
+-------------------------------------------------------------------------------+
| Phase 0: Discovery & Technical Spikes                                         |
|  - PoC: GLRetroView inside AndroidView                                        |
|  - PoC: rcheevos Native Memory Observer & RetroAchievements Spike             |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 1: Core Foundation & Shell App                                          |
|  - Flutter Skeleton & go_router setup                                         |
|  - Drift Database Schema & Room Migration                                     |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 2: mGBA Emulation Kernel & Touch Skins                                  |
|  - mGBA Platform View (`mgba.so`) + MethodChannel Commands                    |
|  - GB, GBC & GBA Touch Overlay Skins                                          |
|  * RUNNABLE MILESTONE: Launch raw GBA ROM into active mGBA session            |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 3: GB/GBC/GBA Game Library & Storage Engine                             |
|  - SAF Directory Scanner & Serial Extractor                                   |
|  - Library Home Screen, Cover Loader & Search                                 |
|  * RUNNABLE MILESTONE: Full ROM discovery, indexing & launch workflow        |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 4: RetroAchievements, Saves & Cheats                                    |
|  - RetroAchievements Auth, Game Hash & In-Game Unlock Banners                 |
|  - Save State Slots (0-9) & Preview Screenshot Store                          |
|  - GameShark / ActionReplay Cheat Parser & Toggling                           |
+-------------------------------------------------------------------------------+
                                       |
                                       v
+-------------------------------------------------------------------------------+
| Phase 5: Handheld Controls, Benchmarking & Release Candidate                  |
|  - Physical Gamepad Mapping & Touch Layout Customizer                         |
|  - Performance Benchmarking (60 FPS verification)                             |
|  * RELEASE CANDIDATE MILESTONE: Production-ready APK                          |
+-------------------------------------------------------------------------------+
```

---

## 2. Granular Phase Breakdown

### Phase 0: Discovery & Risk Reduction (Technical Spikes)

#### Work Item 0.1: GLRetroView AndroidView Embedding Spike
- **Objective**: Prove that `GLRetroView` renders mGBA (`mgba.so`) frames inside Flutter `AndroidView` at 60 FPS without memory leaks.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Test GBA ROM runs continuously at 60 FPS in Flutter `AndroidView`.

#### Work Item 0.2: RetroAchievements `rcheevos` C Memory Spike
- **Objective**: Verify that `rcheevos` C library can inspect mGBA RAM addresses without degrading frame pacing.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Native callback triggers when specific mGBA RAM address changes value.

---

### Phase 1: Core Foundation & Shell App

#### Work Item 1.1: Flutter Repository Setup & Core Shell
- **Objective**: Establish Flutter project structure, Riverpod dependency graph, Material 3 dark theme, and `go_router` paths.
- **Effort Estimate**: **S** (Confidence: High)
- **Acceptance Criteria**: Clean compilation (`flutter analyze`) and basic shell navigation operating.

#### Work Item 1.2: Drift Database Layer & Room Schema Migration
- **Objective**: Implement Drift SQLite schema mirroring legacy Room `RetrogradeDatabase` tables (`games`, `data_files`, `game_cheats`, `achievements`).
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Database initializes; legacy `retrograde.db` opens without data loss.

---

### Phase 2: mGBA Emulation Kernel & Touch Skins

#### Work Item 2.1: Native mGBA MethodChannel & EventChannel Bridge
- **Objective**: Build JNI bridge between Dart `EmulationSessionNotifier` and native `LibretroDroid` loading `mgba.so`.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Reliable execution of `loadGame`, `pauseEmulation`, `resumeEmulation`, and `unloadGame`.

#### Work Item 2.2: Game Boy, GBC & GBA Touch Overlay Skins
- **Objective**: Build virtual touch controls for GB, GBC, and GBA layouts using Flutter `CustomPainter`.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Touch gestures transmit Libretro input bits accurately to mGBA core.

---

### Phase 3: GB/GBC/GBA Game Library & Storage Engine

#### Work Item 3.1: GB/GBC/GBA Directory Scanner & Serial Extractor
- **Objective**: Scan local folders and SAF directories, extract GBA/GBC header serials, and query Libretro `.rdb` files.
- **Effort Estimate**: **L** (Confidence: High)
- **Acceptance Criteria**: Scanner indexes 500+ GB/GBC/GBA ROMs with title and cover matching.

#### Work Item 3.2: Game Library UI Home Screen
- **Objective**: Build responsive game catalog UI showing recent games, system pills (GB, GBC, GBA), search, and cover artwork.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Smooth grid rendering; tapping game launches mGBA emulation session.

---

### Phase 4: RetroAchievements, Saves & Cheats

#### Work Item 4.1: RetroAchievements Authentication & Game Hash Engine
- **Objective**: Authenticate user with RetroAchievements Web API, compute MD5 game hash, and fetch achievement list.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: User logs in; game achievement list downloads and displays locked/unlocked statuses.

#### Work Item 4.2: In-Game Achievement Unlock Banner Overlay
- **Objective**: Listen to native `onAchievementUnlocked` event channel events and render animated unlock toast banners over active mGBA gameplay.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Unlocking achievement displays banner with title, points, and badge icon.

#### Work Item 4.3: Save State Manager & Preview Store
- **Objective**: Manage 10 save slots (0–9) for mGBA, auto-save state generator, and preview screenshots.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: States saved/restored reliably; preview screenshots display in slot sheet.

#### Work Item 4.4: GameShark / ActionReplay Cheat Engine
- **Objective**: Parse `.cht` files and GameShark codes, allowing live toggling via mGBA core.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Toggling cheat updates memory without freezing mGBA core.

---

### Phase 5: Handheld Controls, Benchmarking & Release Candidate

#### Work Item 5.1: Physical Gamepad Mapping & Layout Customizer
- **Objective**: Bind physical handheld controller buttons (Retroid/Odin/Anbernic) and build drag-and-drop touch control layout editor.
- **Effort Estimate**: **M** (Confidence: High)
- **Acceptance Criteria**: Physical buttons control gameplay; customized touch button positions persist.

#### Work Item 5.2: Final Benchmarking & Production Build
- **Objective**: Execute automated test suite, verify sustained 60 FPS output for GBA games, and produce release APK.
- **Effort Estimate**: **S** (Confidence: High)
- **Acceptance Criteria**: 60 FPS performance confirmed; release APK signed and verified.

---

## 3. Relative Effort Summary Matrix

| Phase | Work Item | Focus Area | Effort | Confidence |
| :--- | :--- | :--- | :---: | :---: |
| **Phase 0** | 0.1 | GLRetroView mGBA AndroidView Embedding Spike | **M** | High |
| **Phase 0** | 0.2 | RetroAchievements `rcheevos` C Memory Spike | **M** | High |
| **Phase 1** | 1.1 | Flutter Architecture & Shell Setup | **S** | High |
| **Phase 1** | 1.2 | Drift Database Schema & Room Migration | **M** | High |
| **Phase 2** | 2.1 | Native mGBA JNI MethodChannel Bridge | **M** | High |
| **Phase 2** | 2.2 | GB/GBC/GBA CustomPainter Touch Skins | **M** | High |
| **Phase 3** | 3.1 | SAF Storage Scanner & GBA Serial Extractor | **L** | High |
| **Phase 3** | 3.2 | Game Library UI & Cover Image Caching | **M** | High |
| **Phase 4** | 4.1 | RetroAchievements Auth & Hash Engine | **M** | High |
| **Phase 4** | 4.2 | Achievement Unlock Toast Banner Overlay | **M** | High |
| **Phase 4** | 4.3 | Save State Slot Manager & Previews | **M** | High |
| **Phase 4** | 4.4 | GameShark / ActionReplay Cheat Engine | **M** | High |
| **Phase 5** | 5.1 | Physical Gamepad Mapping & Touch Editor | **M** | High |
| **Phase 5** | 5.2 | Performance Benchmarking & Release Build | **S** | High |
