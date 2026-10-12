# Architecture Decision Records (ADRs)

## Index of Architectural Decisions

- **[ADR-001]: State Management Choice (Flutter Riverpod)**
- **[ADR-002]: Local Database Engine (Drift SQLite)**
- **[ADR-003]: Native Libretro Core Integration Boundary**
- **[ADR-004]: Single Responsive UI for Mobile & Android TV**
- **[ADR-005]: Emulation Core Delivery & Licensing Strategy**

---

## ADR-001: State Management Selection (Flutter Riverpod)

### Context & Problem Statement
Lemuroid requires complex state management across multiple lifecycle scopes: persistent app settings, asynchronous local SQLite database streams, and real-time C++ emulation session runtime states. The previous Kotlin codebase relied on Dagger 2, RxJava, and Kotlin StateFlows across multiple Gradle modules.

### Decision
Adopt **`flutter_riverpod` (v2.x with code generation)** as the universal state management and dependency injection solution for the Flutter application.

### Alternatives Considered
1. **flutter_bloc**: Strong pattern enforcement, but introduces significant event/state boilerplate for simple state reads.
2. **Provider**: Lacks compile-time safety against `ProviderNotFoundException` and struggles with out-of-widget-tree async state scoping.

### Positive Consequences
- **Compile-Time Safety**: Prevents runtime provider lookup failures.
- **Isolate & Testing Friendly**: Overriding providers in tests is straightforward without widget tree mounting (`ProviderContainer`).
- **Clean Architecture Fit**: Decouples business logic (`Notifier`) cleanly from UI Presentation widgets.

### Negative Consequences / Trade-Offs
- Requires team familiarity with Riverpod code-generation syntax (`@riverpod`).

---

## ADR-002: Local Database Selection (Drift SQLite)

### Context & Problem Statement
Existing Lemuroid installations store game catalogs, file checksums, cover URLs, and cheat lists in a Room SQLite database named `retrograde.db`. The target solution must maintain 100% schema migration compatibility to prevent data loss upon upgrade.

### Decision
Adopt **`drift` (type-safe SQLite for Dart)** as the primary persistence engine.

### Alternatives Considered
1. **Isar / Hive (NoSQL)**: High performance, but incompatible with existing `retrograde.db` SQLite files, requiring complex database export/import code.
2. **sqflite**: Direct raw SQL wrapper, but lacks compile-time query verification and code-generated reactive streams.

### Positive Consequences
- **100% Schema Mirroring**: Directly maps to existing Room tables (`games`, `data_files`, `game_cheats`).
- **Type-Safe Queries**: Verifies SQL queries at compile time via Dart code generation.
- **Reactive Streams**: Emits live updates to UI widgets when library records change.

---

## ADR-003: Native Libretro Core Interoperability Strategy

### Context & Problem Statement
Emulation cores (e.g., Snes9x, PCSX-ReARMed, PPSSPP) are written in C/C++ and depend heavily on direct OpenGL/Vulkan rendering and low-latency audio PCM buffer queuing. Reimplementing emulator cores in pure Dart is technically unfeasible.

### Decision
**Retain native C++ core libraries (`.so`) and the `LibretroDroid` JNI engine**, embedding the OpenGL surface in Flutter using `AndroidView` (Hybrid Composition) and communicating via asynchronous `MethodChannel` and `EventChannel`.

### Alternatives Considered
1. **Dart FFI Direct Binding**: Invoking `retro_run()` and binding EGL contexts directly from Dart via FFI.
   * *Rejected*: Dart garbage collection pauses and FFI thread affinity issues lead to audio stuttering and video frame tearing.
2. **Pure Native Activity Switch**: Launching a separate Android NativeActivity for gameplay.
   * *Rejected*: Disrupts Flutter UI overlay navigation, touch skin customization, and in-game cheat/save sheets.

### Positive Consequences
- **Maximum Performance**: OpenGL rendering and audio track queuing remain on optimized C++ threads.
- **Zero Core Regressions**: Preserves Libretro core compatibility across all 20+ supported systems.

---

## ADR-004: Unified Responsive UI for Mobile & Android TV

### Context & Problem Statement
The legacy codebase maintained two completely separate UI trees: Jetpack Compose for Mobile/Tablet (`com.swordfish.lemuroid.app.mobile`) and Android Leanback Fragments for TV (`com.swordfish.lemuroid.app.tv`). This created duplicate maintenance overhead and divergent feature sets.

### Decision
Construct a **single responsive Flutter UI codebase** using `go_router`, auto-detecting TV/Gamepad environments to toggle D-pad focus node highlighting.

### Alternatives Considered
1. **Dual Flutter Entry Points / Packages**: Separate `mobile_app.dart` and `tv_app.dart`.
   * *Rejected*: Re-introduces UI code duplication.

### Positive Consequences
- **Single UI Codebase**: Features implemented once are instantly available on both Mobile and TV.
- **Gamepad Console Support**: Handheld Android consoles (e.g., Odin, Steam Deck, Retroid) gain seamless D-pad support.

---

## ADR-005: Core Delivery & Licensing Model

### Context & Problem Statement
GPL emulator core binaries cannot always be bundled into a single Google Play Store APK due to Google Play policies regarding dynamic code execution and bundle size constraints.

### Decision
Support a dual delivery mechanism:
1. **Bundle Variant (F-Droid / GitHub)**: Pre-packages standard `.so` core libraries inside the app assets.
2. **Dynamic Downloader Variant (Play Store)**: Fetches compiled GPL cores on-demand from official GitHub Release assets into sandboxed internal app storage (`context.filesDir/cores`).

### Positive Consequences
- Guarantees GPL licensing compliance while maintaining Play Store distribution readiness.
