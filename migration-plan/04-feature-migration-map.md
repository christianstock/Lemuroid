# Granular Feature Migration Map

## 1. Overview & Traceability Goal

This document provides a class-level and package-level mapping from the existing Kotlin/Java codebase to the proposed Flutter/Dart feature architecture.

It guarantees functional completeness by demonstrating that every critical business rule, database entity, native bridge call, and user workflow in the existing application has an assigned destination in the new structure.

---

## 2. Core Emulation & Session Boundary Mapping

| Existing Kotlin / C++ Path | Target Dart / Native Path | Disposition | Migration Notes & Changes |
| :--- | :--- | :--- | :--- |
| `com.swordfish.libretrodroid.GLRetroView` | `android/app/src/main/kotlin/.../platform_view/GLRetroViewFactory.kt` | **`Wrap`** | Embedded in Flutter using `AndroidView`. Surface creation and EGL context owned by native Android side. |
| `com.swordfish.libretrodroid.LibretroDroid` | `android/app/src/main/cpp/libretrodroid.cpp` | **`Retain`** | JNI bridge functions (`setCheat`, `resetCheat`, `retro_run`, `loadGame`) remain in native C/C++. |
| `GameViewModelRetroGameView.kt` | `lib/features/emulation_session/state/emulation_session_notifier.dart` | **`Reimplement`** | Converted to Riverpod `EmulationSessionNotifier`. Handles state flow, JNI error handling, and runtime variable updates. |
| `GameViewModelInput.kt` | `lib/features/controller_input/state/input_binding_notifier.dart` | **`Reimplement`** | Translates touch gesture motion events and hardware key codes into Libretro input bits. |
| `GameViewModelTouchControls.kt` | `lib/features/controller_input/presentation/touch_controller_overlay.dart` | **`Reimplement`** | Replaces custom Android layout views with Flutter `CustomPainter` touch controls. |
| `GameViewModelSaves.kt` | `lib/features/save_states/state/save_states_notifier.dart` | **`Reimplement`** | Controls auto-save on backgrounding, slot switching, and preview image rendering. |
| `GameViewModelTilt.kt` & `MotionManager.kt` | `lib/features/emulation_session/data/native_motion_bridge.dart` | **`Reimplement`** | Stream-based sensor events routed to native JNI via EventChannel. |
| `RumbleManager.kt` | `lib/features/emulation_session/data/native_rumble_bridge.dart` | **`Reimplement`** | Calls Flutter `vibration` plugin or native haptic generator upon JNI rumble callback. |

---

## 3. Storage, Scanner & Library Mapping

| Existing Kotlin / Java Path | Target Dart Path | Disposition | Migration Notes & Changes |
| :--- | :--- | :--- | :--- |
| `LocalStorageProvider.kt` & `StorageAccessFrameworkProvider.kt` | `lib/shared/storage/storage_engine.dart` & `android/.../storage/NativeSafHelper.kt` | **`Reimplement` + `Wrap`** | Standard file IO handled in Dart. Fast bulk SAF directory listing delegated to native Kotlin channel. |
| `SerialScanner.kt` & `MagicNumber.kt` | `lib/features/game_library/data/scanner/serial_scanner.dart` | **`Reimplement`** | Ported to pure Dart stream reader for ROM header binary inspection. |
| `LemuroidLibrary.kt` | `lib/features/game_library/data/game_library_repository.dart` | **`Reimplement`** | Orchestrates storage scanning, metadata fetching, and database transaction commits. |
| `LibretroDbParser.kt` | `lib/features/game_library/data/scanner/libretro_db_parser.dart` | **`Reimplement`** | Ported Libretro `.rdb` binary database file parser into async Dart bytes reader. |
| `SkraperMetadataProvider.kt` & `SkraperXmlParser.kt` | `lib/features/game_library/data/metadata/skraper_metadata_provider.dart` | **`Reimplement`** | Replaces Retrofit/XmlPullParser with Dart `http` and `xml` packages. |
| `CoverUtils.kt` | `lib/shared/widgets/game_cover_image.dart` | **`Replace`** | Replaces Coil with Flutter `cached_network_image`. |

---

## 4. Database & Persistence Mapping

| Existing Room Entity / DAO | Target Drift Table / DAO Path | Disposition | Migration Notes & Changes |
| :--- | :--- | :--- | :--- |
| `RetrogradeDatabase.kt` | `lib/shared/database/app_database.dart` | **`Replace`** | Converted to Drift `AppDatabase`. Keeps identical SQLite table names and column types. |
| `Game.kt` & `GameDao.kt` | `lib/features/game_library/data/dao/games_dao.dart` | **`Replace`** | Ported Room `@Query` methods (`getGamesBySystem`, `searchGames`, `getFavorites`) to Drift queries. |
| `DataFile.kt` & `DataFileDao.kt` | `lib/features/game_library/data/dao/data_files_dao.dart` | **`Replace`** | Keeps ROM URI and checksum relationships intact. |
| `GameCheatEntity.kt` & `GameCheatDao.kt` | `lib/features/game_cheats/data/dao/cheats_dao.dart` | **`Replace`** | Stores enabled/disabled cheat codes per game. |
| `Migrations.kt` | `lib/shared/database/database_migrations.dart` | **`Reimplement`** | Handles version upgrades from legacy Room SQLite schema to Drift. |

---

## 5. Saves, Cheats & Auxiliary Feature Mapping

| Existing Class Path | Target Feature Path | Disposition | Migration Notes & Changes |
| :--- | :--- | :--- | :--- |
| `SavesManager.kt` & `StatesManager.kt` | `lib/features/save_states/data/save_states_repository.dart` | **`Reimplement`** | Ported slot path generator (`.state0` to `.state9`), state preview PNG saver, and SRAM flusher. |
| `StatesPreviewManager.kt` | `lib/features/save_states/data/save_preview_store.dart` | **`Reimplement`** | Saves screenshot PNGs adjacent to save state slots for quick preview UI display. |
| `CheatManager.kt` & `CheatDownloader.kt` | `lib/features/game_cheats/data/cheat_repository.dart` | **`Reimplement`** | Downloads Libretro cheat zips, unzips to local storage, and parses `.cht` files. |
| `CwCheatParser.kt` | `lib/features/game_cheats/data/cw_cheat_parser.dart` | **`Reimplement`** | Pure Dart regex parser converting CwCheat strings to Libretro format. |
| `BiosManager.kt` & `Bios.kt` | `lib/features/bios_manager/data/bios_repository.dart` | **`Reimplement`** | Computes MD5 hash of user-provided BIOS files and validates against core manifests. |
| `CoreVariablesManager.kt` & `LemuroidCoreOption.kt` | `lib/features/settings/data/core_options_repository.dart` | **`Reimplement`** | Stores core option overrides (`SharedPreferencesWithCache`) and syncs with JNI runtime. |

---

## 6. Input & Touch Controller Mapping

| Existing Touch / Input Class Path | Target Feature Path | Disposition | Migration Notes & Changes |
| :--- | :--- | :--- | :--- |
| `TouchControllerCustomizer.kt` | `lib/features/controller_input/presentation/touch_editor_screen.dart` | **`Reimplement`** | Interactive Flutter editor allowing drag-and-drop resizing and opacity adjustments of touch controls. |
| `lemuroid-touchinput/radial/*` | `lib/features/controller_input/presentation/widgets/radial_dpad.dart` | **`Reimplement`** | Rebuilt using Flutter `GestureDetector` and `CustomPainter` rendering smooth radial controls. |
| `InputDeviceManager.kt` | `lib/features/controller_input/data/gamepad_manager.dart` | **`Reimplement`** | Listens to physical controller connection/disconnection events via Flutter `HardwareKeyboard`. |
| `InputBindingUpdater.kt` | `lib/features/controller_input/presentation/key_binding_dialog.dart` | **`Reimplement`** | Remaps physical gamepad buttons to Libretro `RETRO_DEVICE_ID_JOYPAD` keys. |

---

## 7. UI Screens & Android TV Navigation Mapping

| Existing Android Screen / Component | Target Flutter Route & Screen | Disposition | Migration Notes & Changes |
| :--- | :--- | :--- | :--- |
| `MainActivity.kt` & Compose Screens | `/library` (`lib/features/game_library/presentation/library_home_screen.dart`) | **`Simplify` & `Reimplement`** | Main home page showing game grid, recent games, favorites, and system filters. |
| `MainTVActivity.kt` & Leanback Fragments | Route auto-adapts to TV via `lib/features/tv_navigation/` | **`Simplify` & `Reimplement`** | **Unified Codebase**: Eliminates Leanback layout duplication. TV mode turns on D-pad Focus Highlighting. |
| `GameActivity.kt` & `TVGameActivity.kt` | `/emulation_session` (`lib/features/emulation_session/presentation/game_screen.dart`) | **`Simplify` & `Reimplement`** | Holds `AndroidView` platform view with overlay menu, touch skins, and fast-forward controls. |
| `CheatMenuScreen.kt` | `lib/features/game_cheats/presentation/cheat_manager_sheet.dart` | **`Reimplement`** | Modal bottom sheet or side panel for searching and toggling game cheats during play. |
| `TVSettingsActivity.kt` & Settings Composables | `/settings` (`lib/features/settings/presentation/settings_screen.dart`) | **`Simplify` & `Reimplement`** | Material 3 preference tiles handling screen filter, HD mode, and audio latency settings. |
| `TVFolderPickerActivity.kt` | Route `/folder_picker` or Flutter `file_picker` sheet | **`Simplify` & `Reimplement`** | Direct directory selector sheet using SAF or native folder dialogs. |

---

## 8. Workers, Injection & Framework Removal

| Existing Component | Target Replacement | Disposition | Migration Notes & Changes |
| :--- | :--- | :--- | :--- |
| `LibraryIndexWork.kt` (WorkManager) | `workmanager` Flutter plugin or Dart Isolate task | **`Wrap`** | Background library rescan scheduled via lightweight native worker bridge. |
| `LemuroidApplicationComponent.kt` (Dagger) | `flutter_riverpod` ProviderScope | **`Remove`** | Fully removed. Replaced by compile-time Riverpod dependency injection. |
| `TimberLoggingHandler.kt` | `lib/core/logger.dart` | **`Replace`** | Replaced with Dart `logger` package with log level controls and native logcat pipe. |
| `Crowdin` Localizations | Flutter `l10n` (`app_en.arb`, `app_es.arb`, etc.) | **`Reimplement`** | Converted Android XML string resources (`strings.xml`) into standard ARB localization files. |
