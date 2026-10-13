# Granular Feature Migration Map

## 1. Traceability Overview

This map establishes direct traceability between existing Kotlin/Java/C++ components and target **GameGrl** Flutter features according to the refined product vision: **Custom Handheld UI (No Material Design)**, **mGBA core focus**, **RetroAchievements**, **Rewind**, **Fast-Forward**, **Solar Light Sensor**, **PDF Game Manuals**, **Front/Back Box Art**, **In-Game Pause Menu**, **Custom Themes**, **LCD Shaders**, **Deep Links**, and **Cache Cleaning**.

---

## 2. Core Emulation, Sensors, Rewind & Shader Mapping

| Existing Component Path | Target Path | Disposition | Migration Notes |
| :--- | :--- | :--- | :--- |
| `lemuroid_core_mgba` (`mgba.so`) | `android/app/src/main/jniLibs/.../libmgba.so` | **`Retain`** | Retained as primary C/C++ core for GB, GBC, and GBA. |
| `MotionManager.kt` (Light Sensor) | `lib/features/emulation_session/data/native_sensor_bridge.dart` | **`Reimplement`** | Maps `Sensor.TYPE_LIGHT` lux to Libretro illuminance sensor 6. |
| `GameViewModelRetroGameView.kt` (Mic) | `lib/features/emulation_session/data/microphone_bridge.dart` | **`Reimplement`** | `RECORD_AUDIO` mic buffer stream passed to mGBA core. |
| `LibretroDroid.java` (Rewind Buffer) | `lib/features/emulation_session/data/rewind_controller.dart` | **`Wrap` & `Reimplement`** | JNI bridge allocating rewind buffer and executing frame rewind steps. |
| `GameActivity.kt` (Fast-Forward) | `lib/features/emulation_session/data/fast_forward_controller.dart` | **`Reimplement`** | mGBA speed multiplier toggle (1.5x–8x). |
| `ShaderChooser.kt` (GLSL Shaders) | `lib/features/shader_engine/data/shader_repository.dart` | **`Wrap`** | EGL GLSL shader selector (`lcd-grid.glsl`, `crt-easymode.glsl`). |
| `GLRetroView.kt` | `android/.../platform_view/GLRetroViewFactory.kt` | **`Wrap`** | Embedded in Flutter using `AndroidView` platform view. |

---

## 3. GameGrl Custom UI & Utility Mapping

| Existing Component Path | Target Path | Disposition | Migration Notes |
| :--- | :--- | :--- | :--- |
| Material Compose Screens | `lib/features/game_library/presentation/gamegrl_carousel_view.dart` | **`Reimplement`** | Replaced with custom 3D Coverflow carousel UI. |
| Compose Details Dialog | `lib/features/game_library/presentation/game_info_card.dart` | **`Reimplement`** | Custom Game Info card displaying game stats and details. |
| `CoverUtils.kt` (Cover Art) | `lib/features/game_library/presentation/box_art_3d_flipper.dart` | **`Reimplement`** | Dual-sided 3D card flipper for Front & Back box art scans. |
| `PdfManualProcessor.kt` | `lib/features/game_manuals/presentation/pdf_manual_viewer_screen.dart` | **`Reimplement`** | Embedded PDF game manual viewer with page zoom. |
| `GameBoyArt.kt` | `lib/features/theme_engine/domain/gameboy_palette.dart` | **`Reimplement`** | Theme engine providing Game Boy LCD color palettes. |
| `GameMenuHelper.kt` | `lib/features/emulation_session/presentation/in_game_pause_menu.dart` | **`Reimplement`** | In-game pause menu with state slots, quick-save, and reset. |
| `DeepLink.kt` | `lib/core/deep_link_handler.dart` | **`Reimplement`** | `gamegrl://launch?gameId=...` URI routing. |
| `CacheCleanerWork.kt` | `lib/shared/storage/cache_cleaner.dart` | **`Reimplement`** | Background cleanup worker for temporary ROM files and thumbs. |

---

## 4. RetroAchievements Subsystem Mapping

| Feature Component | Target Path | Disposition | Implementation Notes |
| :--- | :--- | :--- | :--- |
| **`rcheevos` C Memory Engine** | `android/app/src/main/cpp/rcheevos/` | **`New / Retain`** | Native C library evaluating in-memory triggers against mGBA RAM. |
| **User Login & Session Manager**| `lib/features/retro_achievements/state/retro_achievements_notifier.dart` | **`New / Reimplement`** | Authenticates user with RetroAchievements Web API. |
| **Unlock Overlay Banner** | `lib/features/retro_achievements/presentation/unlock_banner_overlay.dart` | **`New / Reimplement`** | Animated achievement unlock toast overlay during gameplay. |

---

## 5. Android TV & Non-GBA System Removal

| Existing Android TV / Core Component | Target Disposition | Reason for Disposition |
| :--- | :--- | :--- |
| `MainTVActivity.kt` & Leanback UI | **`Remove`** | Android TV support removed from scope. |
| `TVGameActivity.kt` & TV Channels | **`Remove`** | TV channels and activity deleted. |
| Non-GB/GBC/GBA Cores (`pcsx`, `mupen`, `snes9x`, `ppsspp`) | **`Remove`** | Excluded from build configuration to minimize size. |
| Future Cores (`gw`, `melonds`, `citra`) | **`Defer`** | Post-MVP roadmap option for Game & Watch and NDS/3DS. |
