# Architecture Decision Records (ADRs)

## Index of Architectural Decisions

- **[ADR-001]: State Management Selection (Flutter Riverpod)**
- **[ADR-002]: Local Database Engine (Drift SQLite)**
- **[ADR-003]: Custom GameGrl Handheld UI (Scrapping Material Design)**
- **[ADR-004]: Real-Time Frame Rewind & EGL LCD/CRT Shaders**
- **[ADR-005]: mGBA Core Focus & RetroAchievements Integration**
- **[ADR-006]: Solar Light Sensor & Manual Intensity Slider Fallback**

---

## ADR-001: State Management Selection (Flutter Riverpod)

### Context & Problem Statement
GameGrl requires robust state management across multiple UI and emulation scopes: custom 3D carousel indices, PDF manual viewer zoom levels, mGBA real-time frame rewind states, EGL display shader presets, and RetroAchievements user login/unlock notifications.

### Decision
Adopt **`flutter_riverpod` (v2.x with code generation)** as the universal state management and dependency injection solution.

### Positive Consequences
- **Compile-Time Safety**: Prevents runtime provider lookup failures.
- **Clean Architecture Fit**: Decouples business logic (`Notifier`) cleanly from custom UI Presentation widgets.

---

## ADR-002: Local Database Selection (Drift SQLite)

### Context & Problem Statement
Existing Lemuroid installations store game catalogs, file checksums, cover URLs, and cheat lists in a Room SQLite database named `retrograde.db`. GameGrl must maintain 100% schema migration compatibility while expanding table columns to support back box art and PDF manual files.

### Decision
Adopt **`drift` (type-safe SQLite for Dart)** as the primary persistence engine.

### Positive Consequences
- **100% Schema Mirroring & Migration**: Directly maps to existing Room tables (`games`, `data_files`, `game_cheats`) while adding `backCoverUrl` and `manualUrl` columns.
- **Reactive Streams**: Emits live updates to UI widgets when library records change.

---

## ADR-003: Custom GameGrl Handheld UI (Scrapping Material Design)

### Context & Problem Statement
Standard Google Material Design cards and lists feel out of place for a dedicated handheld retro gaming app. GameGrl aims to deliver a premium, custom retro aesthetic tailored specifically for gaming handhelds and smartphones.

### Decision
**Scrap Material Design UI tiles**. Implement a custom-designed handheld UI featuring a **3D Coverflow Game Carousel**, interactive **Game Info Cards**, a **3D Front & Back Box Art Flipper**, an embedded **PDF Game Manual Viewer**, and a **Game Boy LCD Palette Engine**.

### Positive Consequences
- **Bespoke Retro Aesthetic**: Provides a unique, highly polished gaming experience distinct from conventional CRUD apps.
- **Optimized for Handhelds**: Layouts are designed specifically for touchscreens and handheld physical button navigation.

---

## ADR-004: Real-Time Frame Rewind & EGL LCD/CRT Display Shaders

### Context & Problem Statement
Retro gamers expect modern quality-of-life features such as instant frame rewind (to undo mistakes) and authentic display shaders (mimicking Game Boy LCD grids and CRT scanlines).

### Decision
Allocate a **30-second native C++ ring buffer in `LibretroDroid`** for real-time state rewind, and integrate **EGL GLSL display shaders** (`lcd-grid.glsl`, `lcd-dot-matrix.glsl`, `crt-easymode.glsl`) into the `GLRetroView` surface rendering pipeline.

### Positive Consequences
- **Instant Rewind**: Users can hold a rewind button to reverse mGBA gameplay state seamlessly.
- **Authentic Visuals**: Authentic Game Boy LCD grid and CRT scanlines render without frame rate drops.

---

## ADR-005: mGBA Core Focus & RetroAchievements Integration

### Context & Problem Statement
Supporting 20+ disparate emulator cores introduces massive binary bloat and maintenance overhead. The user scope centers on Game Boy (GB, GBC, GBA) emulation and achievement tracking.

### Decision
Focus strictly on the gold-standard **mGBA** (`mgba.so`) Libretro core and integrate the **`rcheevos` C memory observer** directly into the JNI bridge layer to evaluate RetroAchievements RAM triggers in real-time.

### Positive Consequences
- **Streamlined Footprint**: Significantly reduces APK size while delivering solid 60 FPS GBA emulation.
- **RetroAchievements Integration**: Real-time memory polling emits event channel calls that display non-intrusive Flutter unlock banners during gameplay.

---

## ADR-006: Solar Light Sensor & Manual Intensity Slider Fallback

### Context & Problem Statement
Certain GBA games (e.g. *Boktai: The Sun Is in Your Hand*) feature a physical solar sensor built into the game cartridge. When playing on Android devices or handheld consoles without an ambient light sensor (`Sensor.TYPE_LIGHT`), players cannot progress through solar-dependent gameplay mechanics.

### Decision
Bind Android's hardware `Sensor.TYPE_LIGHT` lux readings directly to Libretro sensor ID 6 (`RETRO_SENSOR_ILLUMINANCE`), and provide an **in-game manual Solar Intensity slider** overlay in GameGrl to allow players to manually set the solar light level on handheld devices lacking light sensors.

### Positive Consequences
- **Complete Playability**: *Boktai* and other solar-sensor ROMs remain 100% playable regardless of hardware sensor presence.
