# Granular Feature Migration Map

## 1. Traceability Overview

This map establishes direct traceability between existing Kotlin/Java/C++ components and target Flutter/Dart features according to the refined scope: **Game Boy ecosystem (GB, GBC, GBA via mGBA core)**, **RetroAchievements integration**, and **complete removal of Android TV**.

---

## 2. Core Emulation & Session Boundary Mapping

| Existing Component Path | Target Path | Disposition | Migration Notes |
| :--- | :--- | :--- | :--- |
| `lemuroid_core_mgba` (`mgba.so`) | `android/app/src/main/jniLibs/.../libmgba.so` | **`Retain`** | Retained as primary C/C++ core for GB, GBC, and GBA. |
| Non-GB/GBC/GBA Cores (`pcsx`, `mupen`, `snes9x`, `ppsspp`, etc.) | N/A | **`Remove`** | Completely excluded from build configuration. |
| Future Cores (`gw`, `melonds`, `citra`) | N/A | **`Defer`** | Post-MVP roadmap option for Game & Watch and NDS/3DS. |
| `GLRetroView.kt` | `android/.../platform_view/GLRetroViewFactory.kt` | **`Wrap`** | Embedded in Flutter using `AndroidView` (Hybrid Composition). |
| `GameViewModelRetroGameView.kt` | `lib/features/emulation_session/state/emulation_session_notifier.dart` | **`Reimplement`** | Converted to Riverpod `EmulationSessionNotifier`. |

---

## 3. RetroAchievements Feature Mapping

| Feature Component | Target Path | Disposition | Implementation Notes |
| :--- | :--- | :--- | :--- |
| **`rcheevos` C Memory Engine** | `android/app/src/main/cpp/rcheevos/` | **`New / Retain`** | Native C library evaluating in-memory triggers against mGBA RAM addresses. |
| **User Login & Session Manager**| `lib/features/retro_achievements/state/retro_achievements_notifier.dart` | **`New / Reimplement`** | Authenticates user with RetroAchievements API and stores session token. |
| **Unlock Overlay Banner** | `lib/features/retro_achievements/presentation/unlock_banner_overlay.dart` | **`New / Reimplement`** | Renders non-intrusive achievement unlock toast banners over gameplay. |
| **Achievement Catalog Sheet** | `lib/features/retro_achievements/presentation/achievement_list_sheet.dart` | **`New / Reimplement`** | Displays game achievement list with locked/unlocked status and badge icons. |

---

## 4. Storage, Scanner & Database Mapping (GB / GBC / GBA)

| Existing Component Path | Target Path | Disposition | Migration Notes |
| :--- | :--- | :--- | :--- |
| `LocalStorageProvider.kt` & SAF | `lib/shared/storage/storage_engine.dart` & `android/.../storage/NativeSafHelper.kt` | **`Reimplement` + `Wrap`** | Fast bulk SAF directory reader over platform channel. |
| `SerialScanner.kt` (GBA / GBC header) | `lib/features/game_library/data/scanner/serial_scanner.dart` | **`Reimplement`** | Ported to Dart for reading GBA/GBC game IDs. |
| `RetrogradeDatabase.kt` (Room) | `lib/shared/database/app_database.dart` (Drift) | **`Replace`** | Replaced with Drift SQLite; preserves Room table schema for `retrograde.db`. |

---

## 5. Touch Skins & Input Controller Mapping

| Existing Component Path | Target Path | Disposition | Migration Notes |
| :--- | :--- | :--- | :--- |
| `GameBoyArt.kt` & `GameBoyAdvanceArt.kt` | `lib/features/controller_input/presentation/skins/gba_skin.dart` | **`Reimplement`** | CustomPainter touch overlays for GB, GBC, and GBA layouts. |
| `TouchControllerCustomizer.kt` | `lib/features/controller_input/presentation/touch_editor_screen.dart` | **`Reimplement`** | Drag-and-drop editor for resizing and moving touch controls. |
| `InputDeviceManager.kt` | `lib/features/controller_input/data/gamepad_manager.dart` | **`Reimplement`** | Listens to physical Bluetooth gamepad connections. |

---

## 6. Android TV Removal Mapping

| Existing Android TV Component | Target Disposition | Reason for Disposition |
| :--- | :--- | :--- |
| `MainTVActivity.kt` & Leanback UI | **`Remove`** | Android TV support removed from scope. |
| `TVGameActivity.kt` | **`Remove`** | Handheld / Mobile `GameScreen` handles game session. |
| `TVSearchFragment.kt` & `TVFavoritesFragment.kt` | **`Remove`** | Search/Favorites integrated into Flutter Mobile Library screen. |
| `TVFolderPickerActivity.kt` | **`Remove`** | Mobile folder picker sheet handles SAF directory selection. |
| `ChannelHandler.kt` (Leanback Launcher Channels) | **`Remove`** | TV channel broadcasting omitted. |
