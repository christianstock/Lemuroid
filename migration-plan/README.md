# Lemuroid to Flutter Migration Plan: Index & Guide

## Overview

This directory contains a comprehensive, implementation-ready clean-room migration plan for transitioning the **Lemuroid** Android emulation application from its existing Kotlin/Android Jetpack architecture to a modern, feature-first **Flutter and Dart** cross-platform codebase.

Rather than performing a mechanical 1:1 translation of existing Kotlin packages and Android views, this plan presents a clean-room reimplementation strategy. It preserves all critical emulation capabilities, user data formats, and native performance characteristics while introducing a far more maintainable, modular, and feature-driven software architecture.

---

## Documents in this Migration Plan

| Document | Description |
| :--- | :--- |
| **[01. Executive Overview](./01-executive-overview.md)** | Summary of strategic objectives, architectural vision, key technology selections, native retention strategy, and high-level roadmap. |
| **[02. Existing Application Inventory](./02-existing-application-inventory.md)** | Exhaustive audit of existing modules, screens, domain models, database schemas, background workers, and native dependencies, each tagged with a migration disposition. |
| **[03. Target Architecture](./03-target-architecture.md)** | Feature-first codebase structure, dependency governance rules, state management design, native interop architecture (Platform Channels / `AndroidView` / FFI), persistence, navigation, and TV/Mobile adaptivity. |
| **[04. Feature Migration Map](./04-feature-migration-map.md)** | Traceability matrix mapping Kotlin package/class responsibilities to proposed Flutter features, Dart modules, and platform boundaries. |
| **[05. Phased Implementation Roadmap](./05-phased-implementation-roadmap.md)** | Incremental, dependency-aware implementation phases (Phase 0 through Phase 5) with relative effort estimates, prerequisites, acceptance criteria, and definitions of done. |
| **[06. Risk Register and Technical Spikes](./06-risk-register-and-technical-spikes.md)** | Prioritized technical, performance, and compatibility risks alongside targeted proof-of-concept spike definitions and go/no-go criteria. |
| **[07. Testing and Compatibility Strategy](./07-testing-and-compatibility-strategy.md)** | Multi-tier automated testing strategy, save-state/ROM backward-compatibility validation, native channel testing, and frame latency benchmarking. |
| **[08. Architecture Decision Records (ADRs)](./08-architecture-decision-records.md)** | Formal decision records for major architectural choices (State Management, Native Libretro Interop, Database Persistence, TV UI Strategy, Core Delivery). |
| **[09. Master Task Backlog](./09-master-task-backlog.md)** | Actionable, dependency-linked task backlog with stable identifiers (`TASK-001` through `TASK-050+`) ready for tracking and execution by developers or AI agents. |

---

## Key Strategic Principles

1. **Feature-First Domain Isolation**: Each major domain (Emulation Session, Game Library, Input/Controllers, Saves, Cheats, Settings) owns its UI, business logic, and local data access in a self-contained feature module.
2. **Native Emulation Boundary (`Wrap` & `Retain`)**: Performance-critical Libretro cores and the OpenGL/Vulkan rendering loop stay in optimized C/C++/JNI code via `LibretroDroid`, exposed to Flutter through Android Platform Views (`AndroidView` / `Texture`) and asynchronous Method/Event Channels.
3. **Data & Save Compatibility**: Strict adherence to existing file paths, Room/SQLite database schema migrations, and Libretro `.state` / SRAM file formats to ensure zero loss for upgrading users.
4. **Lean Shared Infrastructure**: Universal services (Storage Access Framework, Network/HTTP, Logging, Theme) reside in clean shared packages without cross-feature pollution.
