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
- **Future Expansion Roadmap**: Game & Watch (`gw`), Nintendo DS / 3DS (`melonds` / `citra`), and **mGBA Local Wi-Fi Link Cable Multiplayer** (post-MVP). All other legacy systems are explicitly removed.
- **Pure Mobile & Handheld Scope**: Android TV codebase deleted. Engineered specifically for smartphones, tablets, and dedicated Android handhelds (e.g., Retroid, Odin, Anbernic).

### 3. Complete Feature Matrix
- **RetroAchievements Integration**: Full support for RetroAchievements.org user authentication, hardcore mode (automatically disables Rewind & Save States), achievement lists, in-game unlock toast overlays, and badge caching.
- **Real-Time Frame Rewind**: Up to 30 seconds of real-time state rewind with a single button press or touch gesture.
- **Fast-Forward Engine**: Speed toggle (1.5x, 2x, 4x, 8x) for mGBA emulation with low-latency audio pitch adjustment.
- **Hardware Ambient Light Sensor**: Hardware light sensor (`Sensor.TYPE_LIGHT`) mapped to Libretro illuminance sensor 6 for GBA solar games (e.g., *Boktai: The Sun Is in Your Hand*). Devices lacking a light sensor simply ignore illuminance (players use solar ROM hacks).
- **Microphone Input (`RECORD_AUDIO`)**: Hardware microphone stream for games requiring mic input.
- **PDF Game Manuals**: Built-in zoomable PDF game manual viewer (`PdfManualProcessor.kt`).
- **Front & Back Box Art**: Dual-sided 3D box art flipper displaying both front cover and back box art scans.
- **In-Game Pause & Session Recovery**: Pause overlay with quick-save, quick-load, 10 save state slots, and reset capabilities.
- **LCD & CRT Display Shaders**: Authentic LCD grid, LCD dot matrix, bilinear filtering, and CRT scanline shader modes (`ShaderChooser.kt`).
- **Deep Linking Launcher**: Direct game launching via URI deep links (`gamegrl://launch?gameId=...`).
- **Automated Cache Cleaner**: Background cleanup worker deleting temporary ROM extraction files and stale preview thumbnails.

---

## Confirmed Architectural Decisions & Open Questions

1. **RetroAchievements Hardcore Mode vs. Rewind Conflict [CONFIRMED]**:
   * *Decision*: GameGrl automatically disables the Rewind button and Save State slots whenever the user enables Hardcore Mode in RetroAchievements settings.
2. **Ambient Light Sensor Handling [CONFIRMED]**:
   * *Decision*: Map hardware `Sensor.TYPE_LIGHT` when supported by the Android device. On handheld devices lacking a light sensor, no fallback UI slider is rendered—players utilize standard solar ROM hacks.
3. **mGBA Local Link Cable / Wi-Fi Multiplayer [CONFIRMED - DEFERRED TO POST-MVP]**:
   * *Decision*: GBA Link Cable multiplayer (for Pokemon trading and head-to-head multiplayer) will be implemented using local Wi-Fi / IP sockets exchanging mGBA SIO register state (similar to My Boy! and Libretro Netplay). Deferred to Phase 5.2 / Post-MVP release to keep v1 focus tight.

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
| **[08. Architecture Decision Records (ADRs)](./08-architecture-decision-records.md)** | Formal ADRs (ADR-001 to ADR-007) covering Custom Handheld UI, State Management, mGBA Focus, RetroAchievements, Rewind/Shaders, Light Sensors, and Wi-Fi Link Cable Multiplayer. |
| **[09. Master Task Backlog](./09-master-task-backlog.md)** | Actionable task checklist (`TASK-001` through `TASK-051`) with dependencies, effort ratings, and acceptance criteria. |
