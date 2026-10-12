# Testing & Compatibility Strategy

## 1. Quality Assurance Vision

Emulation software requires rigorous multi-tier testing: pure Dart business logic, local SQLite persistence, platform channels, native mGBA core execution, RetroAchievements memory triggers, and physical gamepad/touch input rendering.

This strategy establishes a testing framework focused on mGBA handheld performance and RetroAchievements integration correctness.

```
       / \
      /   \     Integration Tests (Android Handheld Hardware & Emulators)
     /  I  \    - Full GBA ROM launch, mGBA JNI lifecycle, RetroAchievements unlock
    /-------\
   /    W    \   Widget & UI Tests (Flutter Test Framework)
  /-----------\  - GB/GBC/GBA touch skins, Achievement unlock toast, Settings tiles
 /      U      \  Pure Unit Tests (Dart + Mocktail)
/---------------\ - GBA serial scanner, CwCheat parser, RetroAchievements API client
```

---

## 2. Multi-Tier Testing Framework

### 2.1 Pure Dart Unit Tests (`test/features/...`)
- **Scope**: Isolated business logic, RetroAchievements API integration, database DAOs, and state notifiers.
- **Tools**: `test`, `mocktail`, `drift_dev/test`.
- **Target Coverage**: **> 85%** for domain models, repositories, and API clients.
- **Key Unit Test Suites**:
  1. `retro_achievements_api_test.dart`: Validates login authentication, game hash matching, and achievement payload deserialization using `mocktail` HTTP client doubles.
  2. `gba_serial_scanner_test.dart`: Verifies header serial extraction (e.g., `AGB-AXVE-USA` for *Pokemon Ruby*) from GBA binary byte streams.
  3. `cw_cheat_parser_test.dart`: Tests GameShark / ActionReplay code string parsing into mGBA memory values.
  4. `drift_database_test.dart`: Executes in-memory SQLite CRUD queries on `games`, `data_files`, `game_cheats`, and `achievements` tables.

### 2.2 Flutter Widget & Screen Tests (`test/widgets/...`)
- **Scope**: UI component layout rendering, touch skin gesture handling, and RetroAchievements unlock banners.
- **Key Widget Test Suites**:
  1. `unlock_banner_overlay_test.dart`: Verifies animated entrance, badge icon display, and automatic dismissal of RetroAchievements unlock toast banners.
  2. `gba_touch_skin_test.dart`: Verifies multi-touch DPAD and L/R shoulder button gesture events.
  3. `achievement_list_sheet_test.dart`: Verifies locked vs unlocked achievement status styling and score calculation.

### 2.3 Platform Channel Mock Testing
- **Scope**: Validating Dart `NativeEmulationBridge` and `RetroAchievementsBridge` platform channel calls.
- **Implementation**:
  ```dart
  void setupMockEmulationChannel() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(
      const MethodChannel('com.swordfish.lemuroid/emulation_command'),
      (MethodCall methodCall) async {
        if (methodCall.method == 'initializeCoreAndGame') {
          return true; // Simulate successful mGBA JNI initialization
        }
        if (methodCall.method == 'initRetroAchievements') {
          return true; // Simulate rcheevos memory hook success
        }
        throw PlatformException(code: 'UNSUPPORTED_METHOD');
      },
    );
  }
  ```

### 2.4 Native Integration Tests (`integration_test/`)
- **Scope**: End-to-end execution on real Android hardware or AVD emulators.
- **Workflow**:
  1. Boot app and authenticate mock RetroAchievements user.
  2. Launch sample homebrew GBA ROM into mGBA `AndroidView`.
  3. Simulate RAM value change triggering achievement condition.
  4. Assert `onAchievementUnlocked` event channel call and UI banner presentation.
  5. Save state to Slot 1, restart mGBA session, and verify state restore.

---

## 3. Backward Compatibility & Data Migration Strategy

### 3.1 Database Migration Verification (`retrograde.db`)
- **Test Objective**: Verify that opening an existing Room database created by Lemuroid Kotlin version 1.17.0 correctly imports all games, cover URLs, and favorite statuses into Drift SQLite without data loss.
- **Validation Script**:
  1. Load seed SQLite database `retrograde_v1.17.0.db`.
  2. Initialize Flutter `AppDatabase` with migration handler.
  3. Query `games` table and assert column schema integrity.

### 3.2 mGBA Save File & Save State Cross-Compatibility
- **Test Objective**: Ensure SRAM save files (`.srm`, `.sav`) and save state binary files (`.state`, `.state0`–`.state9`) created by mGBA core load identically in the Flutter application.
- **Validation Script**:
  1. Copy legacy mGBA save file `PokemonRuby.srm` and state file `PokemonRuby.state1` into target save folder.
  2. Launch game in Flutter app and trigger `loadState(1)`.
  3. Verify that mGBA JNI returns `true` for `retro_unserialize()`.

---

## 4. Performance & Frame Timing Benchmarks

| Metric | Target Threshold | Test Method |
| :--- | :--- | :--- |
| **mGBA Video Frame Rate** | **60 FPS ± 0.2** (16.6 ms/frame) | Android FrameMetrics API / SurfaceFlinger logs |
| **Touch Input Latency** | **< 8 ms** (Touch to mGBA core) | High-speed camera capture / Logcat timestamp delta |
| **rcheevos Memory Check Overhead**| **< 0.5 ms per frame** | Native C++ timer logs during active RAM inspection |
| **ROM Directory Indexing Speed**| **> 150 GBA ROMs / second** | Dart Stopwatch timing during SAF directory traversal |
| **Startup Time (Cold Launch)** | **< 1.0 second** to Library Screen | Flutter Performance Overlay & ADB cold boot trace |

---

## 5. Physical Test Device Matrix

Testing focuses on mobile smartphones, tablets, and dedicated Android handheld gaming devices:

1. **Touchscreen Smartphone**: Modern Android 12+ device (e.g., Google Pixel / Samsung Galaxy) for touch skin gesture and orientation tests.
2. **Android Tablet**: Large screen tablet (e.g., Galaxy Tab) for GBA aspect ratio and touch editor testing.
3. **Dedicated Android Handheld Consoles**:
   - **Retroid Pocket / Ayn Odin**: Handheld consoles with built-in hardware DPAD, A/B/X/Y buttons, L/R shoulder triggers, and Android 10+ OS.
   - **Anbernic RG353V**: Compact 4:3 aspect ratio handheld console testing mGBA display scaling and button mapping.
