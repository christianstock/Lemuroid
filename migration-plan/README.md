# GameGrl Migration Plan: Index & Guide

## Overview

This directory contains a comprehensive, implementation-ready clean-room migration plan for building **GameGrl**—a modern, custom-designed Flutter/Dart handheld emulator for Android, built on top of the **mGBA** (`mgba`) Libretro core and **RetroAchievements.org**.

Rather than performing a mechanical 1:1 translation of existing Kotlin packages or retaining standard Android Material Design tiles, **GameGrl** features a completely custom, bespoke handheld UI designed specifically for retro gaming enthusiasts.

---

## GameGrl Vision & Comprehensive Product Features

### 1. App Identity & Custom Handheld UI
- **Branding**: **GameGrl**
- **Non-Material Custom Aesthetic**: Scraps standard Google Material Design tiles. Features a custom-built handheld interface including an interactive **Game Carousel / Coverflow**, bespoke **Game Info Details Cards**, and a custom **Games View**.
- **Custom Theme Engine**: GameGrl color themes, Game Boy LCD palette switchers (e.g. DMG Green, Pocket Gray, Light Blue, Atomic Purple), and dark OLED skins.

### 2. Core Emulation Focus (mGBA & Game Boy Line)
- **Primary Core**: **mGBA** (`mgba`) delivering gold-standard emulation for **Game Boy (GB)**, **Game Boy Color (GBC)**, and **Game Boy Advance (GBA)**.
- **Future Expansion Roadmap**: Game & Watch (`gw`) and Nintendo DS / 3DS (`melonds` / `citra`). All other legacy systems are explicitly removed.
- **Pure Mobile & Handheld Scope**: Android TV codebase deleted. Engineered specifically for smartphones, tablets, and dedicated Android handhelds (e.g., Retroid, Odin, Anbernic).

### 3. Complete Feature Matrix (Fully Audited from Codebase)
- **RetroAchievements Integration**: Full support for RetroAchievements.org user authentication, hardcode mode, achievement lists, in-game unlock toast overlays, and badge caching.
- **Real-Time Frame Rewind**: Up to 30 seconds of real-time state rewind with a single button press or touch gesture.
- **Fast-Forward Engine**: Speed toggle (1.5x, 2x, 4x, 8x) for mGBA emulation with low-latency audio pitch adjustment.
- **Light & Solar Sensor Input**: Hardware ambient light sensor (`Sensor.TYPE_LIGHT`) mapped to Libretro `RETRO_SENSOR_ILLUMINANCE` (ID 6) for GBA solar games (e.g., *Boktai: The Sun Is in Your Hand*), plus a manual solar intensity slider fallback in the GameGrl overlay menu for handheld devices lacking light sensors.
- **Microphone Input (`RECORD_AUDIO`)**: Hardware microphone stream for games requiring mic input.
- **PDF Game Manuals**: Built-in zoomable PDF game manual viewer (`PdfManualProcessor.kt`).
- **Front & Back Box Art**: Dual-sided 3D box art flipper displaying both front cover and back box art scans.
- **In-Game Pause & Session Recovery**: Pause overlay with quick-save, quick-load, 10 save state slots, and reset capabilities.
- **LCD & CRT Display Shaders**: Authentic LCD grid, LCD dot matrix, bilinear filtering, and CRT scanline shader modes (`ShaderChooser.kt`).
- **Deep Linking Launcher**: Direct game launching via URI deep links (`gamegrl://launch?gameId=...`).
- **Automated Cache Cleaner**: Background cleanup worker deleting temporary ROM extraction files and stale preview thumbnails.

---

## Open Questions & Unresolved Decisions Before Execution

1. **RetroAchievements Hardcore Mode vs. Rewind Conflict**:
   * *Question*: RetroAchievements strictly forbids Rewind and Save States in Hardcore Mode.
   * *Recommendation*: GameGrl will automatically disable the Rewind button and Save State slots whenever the user toggles Hardcore Mode ON in RetroAchievements settings.
2. **Ambient Light Sensor Fallback on Handheld Devices**:
   * *Question*: Many dedicated Android handheld devices (e.g., Retroid Pocket, Anbernic, Ayn Odin) do not contain a physical ambient light sensor (`Sensor.TYPE_LIGHT`).
   * *Recommendation*: Provide an in-game "Solar Brightness" slider overlay in GameGrl for solar-sensor GBA games.
3. **mGBA Local Link Cable Multiplayer**:
   * *Question*: Should GBA local wireless/link cable multiplayer (for Pokemon trading) be included in v1 or deferred?
   * *Recommendation*: Defer GBA Link Cable networking to Phase 5.2 / Post-MVP release to keep v1 focus tight.

---

## Documents in this Migration Plan

| Document | Description |
| :--- | :--- |
| **[01. Executive Overview](./01-executive-overview.md)** | Strategic vision for GameGrl, custom UI aesthetic, mGBA core focus, RetroAchievements, and feature matrix. |
| **[02. Existing Application Inventory](./02-existing-application-inventory.md)** | Audit of existing modules (`lemuroid-app`, `retrograde-app-shared`), mapping manuals, shaders, covers, rewind, sensors, and deep links. |
| **[03. Target Architecture](./03-target-architecture.md)** | Feature-first repository layout (`lib/features/*`), Riverpod state management, Drift persistence, custom GameGrl UI, and native mGBA bridge. |
| **[04. Feature Migration Map](./04-feature-migration-map.md)** | Traceability matrix mapping legacy Kotlin classes to target GameGrl Dart modules and native JNI channels. |
| **[05. Phased Implementation Roadmap](./05-phased-implementation-roadmap.md)** | Incremental phases (Phase 0 through Phase 5) with relative effort estimates, prerequisites, and acceptance criteria. |
| **[06. Risk Register and Technical Spikes](./06-risk-register-and-technical-spikes.md)** | Prioritized technical, mGBA rewind buffer memory, and RetroAchievements risks alongside targeted proof-of-concept spikes. |
| **[07. Testing and Compatibility Strategy](./07-testing-and-compatibility-strategy.md)** | Automated testing strategy, PDF manual viewer tests, LCD shader pipeline benchmarks, and light sensor mocks. |
| **[08. Architecture Decision Records (ADRs)](./08-architecture-decision-records.md)** | Formal ADRs (ADR-001 to ADR-005) covering Custom Handheld UI, State Management, mGBA Focus, RetroAchievements, Rewind/Shaders, and Solar Sensor. |
| **[09. Master Task Backlog](./09-master-task-backlog.md)** | Actionable task checklist (`TASK-001` through `TASK-050`) with dependencies, effort ratings, and acceptance criteria. |
