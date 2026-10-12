# Lemuroid to Flutter Migration Plan: Index & Guide

## Overview

This directory contains a comprehensive, implementation-ready clean-room migration plan for transitioning the **Lemuroid** Android emulation application from its existing Kotlin/Android Jetpack architecture to a modern, feature-first **Flutter and Dart** cross-platform codebase.

Rather than performing a mechanical 1:1 translation of existing Kotlin packages and Android views, this plan presents a clean-room reimplementation strategy tailored to a refined product scope.

---

## Refined Product Scope & Vision

Based on updated requirements, the Flutter reimplementation adopts the following focused product scope:

1. **Target Systems (Core Focus)**:
   - **Game Boy (GB)**, **Game Boy Color (GBC)**, and **Game Boy Advance (GBA)** using the **mGBA** (`mgba`) Libretro core.
   - *Future Expansion Roadmap*: Game & Watch (`gw`) and Nintendo DS / 3DS (`melonds` / `citra`). All other legacy systems (PSX, N64, PSP, Arcade, Atari, etc.) are explicitly dropped.
2. **RetroAchievements Integration**:
   - Full support for [RetroAchievements.org](https://retroachievements.org/) login, achievement progress tracking, unlock popups/overlays, hardcode mode, and badge image caching.
3. **Mobile & Handheld Focus (No TV)**:
   - **Android TV support is dropped entirely**. The UI is built specifically for mobile smartphones, tablets, and Android handheld gaming consoles (e.g., Retroid Pocket, Ayn Odin, Anbernic) with touch skins and physical controller mapping.

---

## Documents in this Migration Plan

| Document | Description |
| :--- | :--- |
| **[01. Executive Overview](./01-executive-overview.md)** | Summary of strategic objectives, mGBA core focus, RetroAchievements integration, TV removal, and roadmap. |
| **[02. Existing Application Inventory](./02-existing-application-inventory.md)** | Audit of existing modules, domain models, database schemas, and native dependencies with updated migration dispositions. |
| **[03. Target Architecture](./03-target-architecture.md)** | Feature-first codebase structure (`lib/features/*`), dependency governance rules, Riverpod state management, native mGBA JNI interop, RetroAchievements domain, and Drift persistence. |
| **[04. Feature Migration Map](./04-feature-migration-map.md)** | Traceability matrix mapping legacy Kotlin source packages and classes to Dart features, packages, and native plugins. |
| **[05. Phased Implementation Roadmap](./05-phased-implementation-roadmap.md)** | Incremental, dependency-aware implementation phases (Phase 0 through Phase 5) with relative effort estimates, prerequisites, and acceptance criteria. |
| **[06. Risk Register and Technical Spikes](./06-risk-register-and-technical-spikes.md)** | Prioritized technical, performance, and RetroAchievements API risks alongside targeted proof-of-concept spikes and go/no-go gates. |
| **[07. Testing and Compatibility Strategy](./07-testing-and-compatibility-strategy.md)** | Multi-tier automated testing strategy, RetroAchievements mock API tests, mGBA save-state compatibility validation, and frame latency benchmarking. |
| **[08. Architecture Decision Records (ADRs)](./08-architecture-decision-records.md)** | Formal decision records for major architectural choices (State Management, mGBA Core Focus, RetroAchievements Integration, TV Removal, Database Persistence). |
| **[09. Master Task Backlog](./09-master-task-backlog.md)** | Actionable, dependency-linked task backlog with stable identifiers (`TASK-001` through `TASK-050`) ready for tracking and execution. |

---

## Key Strategic Principles

1. **Focused Emulation Boundary (`mGBA`)**: Emulation is streamlined around the highly optimized `mgba` core for GB, GBC, and GBA games.
2. **RetroAchievements Native Binding**: Native `rcheevos` C library calls and HTTP API hooks integrated with Flutter overlay notifications.
3. **Data & Save Compatibility**: Strict adherence to existing SRAM (`.srm`) and Libretro `.state` formats to guarantee zero save loss.
4. **Pure Handheld Experience**: Dedicated mobile and gaming handheld UI without bloated TV navigation code.
