# Architecture Decision Records (ADRs)

## Index of Architectural Decisions

- **[ADR-001]: State Management Selection (Flutter Riverpod)**
- **[ADR-002]: Local Database Engine (Drift SQLite)**
- **[ADR-003]: mGBA Core Focus & Native JNI Boundary**
- **[ADR-004]: RetroAchievements Architecture (`rcheevos` C Library)**
- **[ADR-005]: Complete Removal of Android TV Support**

---

## ADR-001: State Management Selection (Flutter Riverpod)

### Context & Problem Statement
Lemuroid requires complex state management across multiple lifecycle scopes: persistent app settings, asynchronous local SQLite database streams, real-time mGBA emulation session states, and RetroAchievements user login/unlock notifications.

### Decision
Adopt **`flutter_riverpod` (v2.x with code generation)** as the universal state management and dependency injection solution.

### Alternatives Considered
1. **flutter_bloc**: Strong pattern enforcement, but introduces significant event/state boilerplate.
2. **Provider**: Lacks compile-time safety against `ProviderNotFoundException`.

### Positive Consequences
- **Compile-Time Safety**: Prevents runtime provider lookup failures.
- **Clean Architecture Fit**: Decouples business logic (`Notifier`) cleanly from UI Presentation widgets.

---

## ADR-002: Local Database Selection (Drift SQLite)

### Context & Problem Statement
Existing Lemuroid installations store game catalogs, file checksums, cover URLs, and cheat lists in a Room SQLite database named `retrograde.db`. The target solution must maintain 100% schema migration compatibility to prevent data loss upon upgrade.

### Decision
Adopt **`drift` (type-safe SQLite for Dart)** as the primary persistence engine.

### Positive Consequences
- **100% Schema Mirroring**: Directly maps to existing Room tables (`games`, `data_files`, `game_cheats`) while adding an `achievements` cache table.
- **Reactive Streams**: Emits live updates to UI widgets when library records change.

---

## ADR-003: mGBA Core Focus & Native JNI Boundary

### Context & Problem Statement
The product scope focuses strictly on Game Boy (GB), Game Boy Color (GBC), and Game Boy Advance (GBA) games. Reimplementing emulator cores in Dart is technically unfeasible due to frame timing constraints.

### Decision
**Retain the native mGBA core binary (`mgba.so`) and `LibretroDroid` JNI engine**, embedding the OpenGL surface in Flutter using `AndroidView` (Hybrid Composition).

### Alternatives Considered
1. **Bundling 20+ Cores**: Keep PSX, N64, PSP, Arcade, etc.
   * *Rejected*: Dropped per user requirement to eliminate code bloat and streamline handheld mGBA performance.

### Positive Consequences
- **Maximum Performance**: mGBA core renders GBA games at a solid 60 FPS with minimal battery drain and sub-8ms touch latency.

---

## ADR-004: RetroAchievements Architecture (`rcheevos` C Library)

### Context & Problem Statement
Integrating RetroAchievements requires authenticating users via HTTP APIs, computing game MD5 hashes, downloading achievement definitions, and inspecting live mGBA RAM addresses in real-time during gameplay.

### Decision
Integrate the native **`rcheevos` C library** directly into the JNI bridge layer to observe mGBA RAM addresses, and use a Dart `RetroAchievementsApiClient` to handle user login and badge thumbnail downloads.

### Positive Consequences
- **Zero Frame Stutter**: RAM condition checks execute in native C code without pausing mGBA's `retro_run()` loop.
- **In-Game Overlay**: Unlocking achievements emits an event channel payload that renders a Flutter toast overlay on top of active gameplay.

---

## ADR-005: Complete Removal of Android TV Support

### Context & Problem Statement
The legacy codebase maintained a separate Android TV codebase using Leanback Fragments (`com.swordfish.lemuroid.app.tv`), creating duplicate maintenance overhead.

### Decision
**Completely remove Android TV support** from the migration scope. The Flutter UI is engineered exclusively for touchscreen smartphones, tablets, and dedicated Android handheld consoles (e.g., Retroid, Odin).

### Positive Consequences
- **60% Code Reduction**: Deletes TV activities, TV search providers, TV folder pickers, and Leanback channel broadcasters.
- **Focused Handheld UX**: Engineering efforts concentrate on physical gamepad remapping and GB/GBC/GBA touch skins.
