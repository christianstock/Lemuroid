# Testing & Compatibility Strategy

## 1. Quality Assurance Vision

Building **GameGrl** requires comprehensive testing across pure Dart logic, custom handheld UI widgets, local SQLite persistence, platform channels, native mGBA core execution, real-time frame rewind, solar light sensors, LCD shaders, and PDF game manual rendering.

```
       / \
      /   \     Integration Tests (Android Handheld Hardware & Emulators)
     /  I  \    - Full GBA ROM launch, mGBA JNI rewind, Boktai solar sensor, RetroAchievements
    /-------\
   /    W    \   Widget & UI Tests (GameGrl Custom UI)
  /-----------\  - 3D Game Carousel, Front/Back Box Art Flipper, PDF Manual Viewer
 /      U      \  Pure Unit Tests (Dart + Mocktail)
/---------------\ - GBA serial scanner, Light sensor bridge, Rewind buffer, Drift DB
```

---

## 2. Multi-Tier Testing Framework

### 2.1 Pure Dart Unit Tests (`test/features/...`)
- **Key Unit Test Suites**:
  1. `retro_achievements_api_test.dart`: Validates login authentication, game hash matching, and achievement payload deserialization.
  2. `native_sensor_bridge_test.dart`: Verifies ambient light sensor (`Sensor.TYPE_LIGHT`) lux conversion to Libretro illuminance sensor ID 6.
  3. `rewind_controller_test.dart`: Verifies rewind step count calculations and frame delta math.
  4. `gba_serial_scanner_test.dart`: Verifies header serial extraction (e.g., `AGB-AXVE-USA` for *Pokemon Ruby*).
  5. `drift_database_test.dart`: Tests SQLite CRUD queries for games, front/back cover URLs, manuals, and achievements.

### 2.2 GameGrl Custom Widget & UI Tests (`test/widgets/...`)
- **Key Widget Test Suites**:
  1. `gamegrl_carousel_view_test.dart`: Verifies custom 3D carousel scroll gesture handling and active item focus.
  2. `box_art_3d_flipper_test.dart`: Verifies tap-to-flip gesture animating 3D matrix transform between front cover and back box art.
  3. `pdf_manual_viewer_test.dart`: Verifies PDF document loading, page zoom, and bookmark navigation.
  4. `unlock_banner_overlay_test.dart`: Verifies animated entrance and dismissal of RetroAchievements unlock toast banners over mGBA gameplay surface.

### 2.3 Platform Channel Mock Testing
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
        if (methodCall.method == 'sendSensorEvent') {
          final sensorId = methodCall.arguments['sensorId'];
          return sensorId == 6; // Verify light sensor ID 6
        }
        if (methodCall.method == 'rewindStep') {
          return true; // Simulate mGBA rewind step execution
        }
        if (methodCall.method == 'setShader') {
          return true; // Simulate EGL GLSL LCD shader loading
        }
        throw PlatformException(code: 'UNSUPPORTED_METHOD');
      },
    );
  }
  ```

---

## 3. Backward Compatibility & Data Migration Strategy

### 3.1 Database Migration Verification (`retrograde.db`)
- **Test Objective**: Verify that opening an existing Room database created by Lemuroid Kotlin version 1.17.0 correctly imports all games, cover URLs, and favorite statuses into Drift SQLite without data loss while adding `backCoverUrl` and `manualUrl`.

### 3.2 mGBA Save File & Save State Cross-Compatibility
- **Test Objective**: Ensure SRAM save files (`.srm`, `.sav`) and save state binary files (`.state`, `.state0`–`.state9`) created by mGBA core load identically in GameGrl.

---

## 4. Performance & Frame Timing Benchmarks

| Metric | Target Threshold | Test Method |
| :--- | :--- | :--- |
| **mGBA Video Frame Rate** | **60 FPS ± 0.2** (16.6 ms/frame) | Android FrameMetrics API / SurfaceFlinger logs |
| **Real-Time Frame Rewind** | **60 FPS** during active rewind | Native JNI timestamp delta during `rewindStep` |
| **LCD Shader GPU Render Time** | **< 4.0 ms per frame** | OpenGL ES frame timer during GLSL shader pass |
| **Touch Input Latency** | **< 8 ms** (Touch to mGBA core) | High-speed camera capture / Logcat timestamp delta |
| **3D Box Art Flip Frame Rate** | **60 FPS** smooth transform | Flutter Performance Overlay during card flip |

---

## 5. Physical Test Device Matrix

1. **Touchscreen Smartphone**: Modern Android 12+ device (e.g., Google Pixel / Samsung Galaxy) testing GameGrl custom carousel, PDF manual viewer, hardware light sensor, and GBA touch skins.
2. **Dedicated Android Handheld Consoles**:
   - **Retroid Pocket / Ayn Odin**: Handheld consoles with built-in hardware DPAD, A/B/X/Y buttons, L/R shoulder triggers, testing mGBA 60 FPS performance with LCD shaders.
   - **Anbernic RG353V**: Compact handheld console testing Game Boy LCD color palettes (DMG Green, Pocket Gray) and physical button mapping.
