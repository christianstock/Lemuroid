# Lemuroid Feature Enhancement - Feasibility Study

## Project Overview
Lemuroid is an Android emulator based on LibretroDroid (a LibRetro wrapper library). The codebase is well-structured with:
- **Core library**: LibretroDroid (handles emulation cores)
- **Touch controls**: Compose-based radial touch controller system
- **Architecture**: Kotlin, MVVM pattern, Jetpack Compose UI
- **Systems**: Supports GB, GBC, GBA, and many other systems

---

## A) CHEAT SCANNER & ACTIVATOR FOR GBA/GB/GBC

### Current State
- **NO existing cheat support** in Lemuroid
- LibRetro cores (Gambatte for GB/GBC, mGBA for GBA) support cheats natively
- LibretroDroid wrapper likely has cheat API (needs verification)

### Technical Requirements

#### 1. **Database Layer** (MEDIUM effort)
**Files to create:**
- `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/cheats/CheatDatabase.kt`
- `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/cheats/entity/Cheat.kt`
- `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/cheats/dao/CheatDao.kt`

**Schema:**
```kotlin
@Entity(tableName = "cheats")
data class Cheat(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val gameId: String,           // Links to Game table
    val code: String,              // Cheat code (GameShark, Action Replay, etc.)
    val description: String,
    val enabled: Boolean = false,
    val codeType: String          // "gamegenie", "gameshark", "codebreaker"
)
```

#### 2. **LibRetro Cheat Database Parser** (MEDIUM-HIGH effort)
**Files to create:**
- `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/cheats/LibretroCheatParser.kt`

**Tasks:**
- Parse LibRetro `.cht` files (they use a specific format)
- Match cheats to games by CRC32/Serial/Name
- Bulk import functionality
- Filter by system (GB, GBC, GBA)

**LibRetro Cheat Format Example:**
```
cheats = 4

cheat0_desc = "Infinite Health"
cheat0_code = "00E05B:63"
cheat0_enable = false

cheat1_desc = "Max Gold"
cheat1_code = "00E06A:FF+00E06B:FF"
cheat1_enable = false
```

#### 3. **LibretroDroid Integration** (HIGH effort - requires library investigation)
**Files to modify:**
- `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/shared/game/viewmodel/GameViewModelRetroGameView.kt`

**Investigation needed:**
- Check if LibretroDroid exposes `retro_cheat_reset()`, `retro_cheat_set()` APIs
- If not, might need to fork LibretroDroid library
- Alternative: Use JNI to call LibRetro core functions directly

**Integration points:**
```kotlin
// When loading game
retroGameView.clearCheats()
enabledCheats.forEach { cheat ->
    retroGameView.setCheat(cheat.code, cheat.description)
}
```

#### 4. **UI Components** (MEDIUM effort)
**Files to create:**
- `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/cheats/CheatListScreen.kt`
- `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/cheats/CheatImportScreen.kt`

**Integration points:**
- Add "Cheats" option in game menu ([GameMenuHomeScreen.kt](lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/gamemenu/GameMenuHomeScreen.kt))
- Add bulk import in settings
- Toggle cheats on/off during gameplay

#### 5. **Files to Modify:**
1. **Database schema:** `retrograde-app-shared/src/main/java/com/swordfish/lemuroid/lib/library/db/RetrogradeDatabase.kt`
   - Add CheatDao, bump version, add migration
2. **Game menu:** `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/gamemenu/GameMenuHomeScreen.kt`
   - Add cheat menu item
3. **ViewModel:** `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/shared/game/viewmodel/GameViewModelRetroGameView.kt`
   - Load/apply cheats when game starts

### Effort Estimate
- **Database & Parser:** 3-5 days
- **LibretroDroid investigation & integration:** 5-10 days (depends on API availability)
- **UI development:** 3-4 days
- **Testing:** 2-3 days
- **Total:** **13-22 days** (2-4 weeks)

### Complexity: **MEDIUM-HIGH**
Main challenge: LibretroDroid might not expose cheat APIs. May need library modification.

---

## B) BOX ART / COVER VISUALIZATION

### Current State
- ✅ Already implemented!
- Uses LibRetro database for metadata
- Fetches thumbnails from web
- Grid/list view with cover art

**Location:** 
- `lemuroid-metadata-libretro-db/src/main/java/com/swordfish/lemuroid/metadata/libretrodb/LibretroDBMetadataProvider.kt`
- Computes cover URLs based on game name

### Recommendation
**No changes needed** - box art system is already good. Possible enhancements:
- Higher resolution covers
- 3D box art option
- Custom cover uploads

### Effort Estimate: **0 days** (already done)

---

## C) CUSTOMIZABLE UI TO LOOK LIKE REAL GAME BOY / GBA

### Current State
- Touch controls use Jetpack Compose with configurable layouts
- Existing GB/GBC/GBA layouts: 
  - `lemuroid-touchinput/src/main/java/com/swordfish/touchinput/radial/layouts/GB.kt`
  - `lemuroid-touchinput/src/main/java/com/swordfish/touchinput/radial/layouts/GBA.kt`
- Current theme: `LemuroidPadTheme.kt` (modern glass design)

### Technical Requirements

#### 1. **Themed UI Skins** (HIGH effort)
**Files to create:**
- `lemuroid-touchinput/src/main/java/com/swordfish/touchinput/radial/themes/GameBoyClassicTheme.kt`
- `lemuroid-touchinput/src/main/java/com/swordfish/touchinput/radial/themes/GameBoyColorTheme.kt`
- `lemuroid-touchinput/src/main/java/com/swordfish/touchinput/radial/themes/GameBoyAdvanceTheme.kt`

**Components needed:**
- Realistic button textures/shapes (circular GB buttons vs oval GBA)
- D-pad with authentic shape
- System-specific colors (DMG-01 gray, GBC purple/clear, GBA purple/platinum)
- Background bezel/frame mimicking real hardware
- Screen border with authentic proportions

#### 2. **Screen Frame Overlay** (MEDIUM effort)
**Files to modify:**
- `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/game/MobileGameScreen.kt`

**Implementation:**
- Add border/bezel overlays around game viewport
- Different bezels for GB (DMG-01 gray), GBC (purple), GBA (wide screen)
- Make semi-transparent to see game behind

#### 3. **Color Palette Customization** (MEDIUM effort)
**Files to create:**
- `lemuroid-touchinput/src/main/java/com/swordfish/touchinput/radial/themes/ColorPalette.kt`

**Options:**
- GB: DMG-01 (gray), Pocket (silver), Light (gold)
- GBC: Purple, Teal, Atomic Purple, clear variants
- GBA: Purple, Platinum, Famicom edition, SP variants
- Custom color picker for buttons/shell

#### 4. **Layout Profiles** (MEDIUM effort)
**Files to modify:**
- `lemuroid-touchinput/src/main/java/com/swordfish/touchinput/radial/settings/TouchControllerSettingsManager.kt`

**Features:**
- Save/load themed presets
- Per-system theme persistence
- Quick theme switcher in game menu

### Files to Modify:
1. **Touch controller layouts:** `lemuroid-touchinput/src/main/java/com/swordfish/touchinput/radial/layouts/*.kt`
   - Redesign button shapes/positions for authenticity
2. **Theme system:** `lemuroid-touchinput/src/main/java/com/swordfish/touchinput/radial/LemuroidPadTheme.kt`
   - Add theme enum, color schemes
3. **Game screen:** `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/game/MobileGameScreen.kt`
   - Add bezel overlay composable
4. **Settings:** Add theme picker UI

### Assets Needed:
- Button textures (PNG with transparency)
- D-pad texture
- Bezel frames for each system
- Reference photos of real hardware

### Effort Estimate
- **Theme architecture:** 2-3 days
- **GB classic theme:** 4-5 days (design + implementation)
- **GBC theme:** 3-4 days
- **GBA theme:** 3-4 days
- **Color customization UI:** 2-3 days
- **Bezel overlays:** 3-4 days
- **Testing/polish:** 3-4 days
- **Total:** **20-27 days** (4-5 weeks)

### Complexity: **HIGH** (design-intensive, requires pixel-perfect authenticity)

---

## D) FULLY CUSTOMIZABLE CONTROLS

### Current State
- Touch controls are semi-customizable:
  - Scale, rotation, margins (X/Y position)
  - **Location:** `TouchControllerSettingsManager.kt`
- Controls are radial dials (not individually movable)
- Fixed layouts per system

### Technical Requirements

#### 1. **Individual Button Positioning** (HIGH effort)
**Current limitation:** Buttons are grouped in "dials" (left/right clusters)

**Files to modify:**
- `lemuroid-touchinput/src/main/java/com/swordfish/touchinput/radial/layouts/*.kt`
- Entire PadKit library architecture

**New architecture needed:**
- Free-form button positioning (not constrained to dials)
- Each button stores: X, Y, scale, rotation, opacity
- Collision detection to prevent overlaps

**Files to create:**
- `lemuroid-touchinput/src/main/java/com/swordfish/touchinput/freeform/FreeformLayout.kt`
- `lemuroid-touchinput/src/main/java/com/swordfish/touchinput/freeform/DraggableButton.kt`

#### 2. **Button Remapping** (MEDIUM effort)
**Files to create:**
- `lemuroid-touchinput/src/main/java/com/swordfish/touchinput/controller/ButtonMapper.kt`

**Features:**
- Map any button to any key code
- Multiple buttons to same action
- Macro support (one button = multiple inputs)

**Database schema:**
```kotlin
@Entity(tableName = "button_mappings")
data class ButtonMapping(
    val layoutId: String,
    val buttonId: String,
    val targetKeyCode: Int,
    val x: Float,
    val y: Float,
    val scale: Float,
    val rotation: Float
)
```

#### 3. **Per-Game Control Profiles** (MEDIUM effort)
**Files to create:**
- `lemuroid-touchinput/src/main/java/com/swordfish/touchinput/profiles/ControlProfile.kt`

**Features:**
- Save named profiles (e.g., "Doom Layout", "Pokémon Portrait")
- Auto-load profile per game
- Share profiles as JSON files

#### 4. **Visual Editor** (VERY HIGH effort)
**Files to create:**
- `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/controls/ControlEditorScreen.kt`

**Features:**
- Drag-and-drop buttons
- Pinch-to-scale individual buttons
- Rotation handles
- Grid snapping
- Button library/palette
- Preview with game running

### Files to Modify:
1. **PadKit integration:** Entire `lemuroid-touchinput/` module
   - Refactor from dial-based to free-form
2. **Game screen:** `MobileGameScreen.kt`
   - Support dynamic button layouts
3. **Settings manager:** `TouchControllerSettingsManager.kt`
   - Store per-button settings (not just global)
4. **Database:** Add button mapping tables

### Effort Estimate
- **Architecture refactor (dial → free-form):** 7-10 days
- **Individual button positioning:** 5-7 days
- **Button remapping system:** 3-4 days
- **Visual editor UI:** 8-12 days
- **Profile management:** 3-4 days
- **Per-game profiles:** 2-3 days
- **Testing (controls are critical!):** 5-7 days
- **Total:** **33-47 days** (7-9 weeks)

### Complexity: **VERY HIGH**
Major architectural change. Current PadKit system not designed for this. Essentially rebuilding touch controls.

---

## E) AUTHENTIC SCREEN RENDERING (GB/GBC/GBA)

### Current State
- Shader system exists: `ShaderChooser.kt`
- Options: CRT, LCD, Smooth, Sharp, HD upscaling
- Default for GB/GBC/GBA: `ShaderConfig.LCD`
- LibRetroDroid supports custom shaders

### Technical Requirements

#### 1. **Screen Dimension Matching** (EASY effort)
**Current:** Game viewport scales to fill screen

**Files to modify:**
- `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/game/MobileGameScreen.kt`

**Implementation:**
- Add aspect ratio lock option
- Pillarbox/letterbox to maintain original dimensions
- GB: 160x144, GBC: 160x144, GBA: 240x160

**Changes needed:**
```kotlin
// In MobileGameScreen.kt
val aspectRatio = when (system) {
    SystemID.GB, SystemID.GBC -> 160f / 144f
    SystemID.GBA -> 240f / 160f
    else -> null
}

// Apply to GLRetroView layout
```

#### 2. **Authentic LCD Shader** (MEDIUM effort)
**Current LCD shader:** Generic, may not match real hardware

**Files to create:**
- Custom GLSL shaders for each system
- `lemuroid-app/src/main/assets/shaders/gb_dmg.glsl` (DMG-01 green tint, pixel grid)
- `lemuroid-app/src/main/assets/shaders/gbc_screen.glsl` (color LCD, slight blur)
- `lemuroid-app/src/main/assets/shaders/gba_screen.glsl` (backlit LCD, ghosting effect)

**Features:**
- Pixel grid overlay (visible sub-pixels)
- LCD ghosting/motion blur (especially GBA)
- Color temperature matching
- Screen reflections (optional)
- Backlight simulation (off/on/washed out)

**Files to modify:**
- `ShaderChooser.kt` - Add authentic LCD presets
- `GameViewModelRetroGameView.kt` - Apply system-specific shaders

#### 3. **Color Palettes (GB only)** (EASY effort)
**Already exists!** GB system has palette options:
```kotlin
// In GameSystem.kt (GB core config)
ExposedSetting(
    "gambatte_gb_colorization",
    R.string.setting_gambatte_gb_colorization,
),
ExposedSetting(
    "gambatte_gb_internal_palette",
    R.string.setting_gambatte_gb_internal_palette,
),
```

**Enhancement:** Add more palette presets
- Add UI for palette preview
- Custom palette creator (RGB picker for 4 shades)

#### 4. **Screen Mode Options** (EASY effort)
**Files to create:**
- Settings for LCD simulation strength

**Options:**
- Pixel grid intensity (0-100%)
- Ghosting strength (for GBA)
- Color temperature adjustment
- Scanlines (if desired)
- Backlight brightness (DMG=none, GBA=bright, GBA SP=brightest)

### Files to Modify:
1. **Shader system:** `ShaderChooser.kt`
   - Add `ShaderConfig.GB_AUTHENTIC`, `ShaderConfig.GBA_AUTHENTIC`
2. **Game screen:** `MobileGameScreen.kt`
   - Aspect ratio locking
3. **Settings:** Add LCD simulation options
4. **Shaders:** Create GLSL shader files (if not using LibRetro built-ins)

### Shader Resources:
- LibRetro shader repository: https://github.com/libretro/glsl-shaders
- Pre-made shaders: `lcd-cgwg`, `handheld/lcd3x`, `dot`

### Effort Estimate
- **Aspect ratio locking:** 1 day
- **Research/test existing LibRetro shaders:** 2-3 days
- **Custom shader development (if needed):** 5-7 days
- **UI for screen options:** 2-3 days
- **Testing/calibration:** 2-3 days
- **Total:** **12-17 days** (2.5-3.5 weeks)

### Complexity: **MEDIUM**
Shaders are the main challenge. LibRetro likely has most of what's needed already.

---

## OVERALL SUMMARY

| Feature | Effort (days) | Complexity | Blockers |
|---------|--------------|------------|----------|
| **A) Cheat System** | 13-22 | MEDIUM-HIGH | LibretroDroid API may need extension |
| **B) Box Art** | 0 | - | Already done ✅ |
| **C) Themed UI** | 20-27 | HIGH | Design-intensive, needs assets |
| **D) Custom Controls** | 33-47 | VERY HIGH | Architectural refactor needed |
| **E) Authentic Screen** | 12-17 | MEDIUM | Shader knowledge required |
| **TOTAL** | **78-113 days** | | |

**Total Estimate: 3.5-5.5 months** of full-time development

---

## DEVELOPMENT ENVIRONMENT

### Do You Need Android Studio?

**Short answer: Yes, highly recommended, but VS Code *could* work.**

#### Android Studio (Recommended ✅)
**Pros:**
- Best Android development experience
- Built-in emulator for testing
- Layout inspector, profiler
- Gradle integration works perfectly
- Compose preview for UI work

**Cons:**
- Heavy IDE (8GB+ RAM recommended)

#### VS Code (Possible but Limited ⚠️)
**Pros:**
- Lighter weight
- Good for Kotlin editing

**Cons:**
- No Compose preview
- Must use command-line Gradle
- No layout inspector
- Testing requires physical device or external emulator
- Debugging is harder

**Setup for VS Code:**
1. Install Android SDK via command line
2. Install Kotlin extension
3. Install Gradle extension
4. Use `./gradlew assembleDebug` to build
5. Use `adb install` to deploy
6. Debugging via `adb logcat`

### Recommended Setup
- **Android Studio Hedgehog or later** (2024+)
- **JDK 17** (already configured in project)
- **Physical Android device** for testing touch controls (emulator touch is different)
- **Device with Android 8.0+**

### Build Commands
```bash
# Build debug APK
./gradlew lemuroid-app:assembleDebug

# Install on device
./gradlew lemuroid-app:installDebug

# Run tests
./gradlew test
```

---

## RECOMMENDED APPROACH

### Phase 1: Quick Wins (2-3 weeks)
1. **Authentic screen rendering** (E) - Most visible improvement, moderate effort
2. **Box art** - Already done, just document

### Phase 2: Medium Complexity (4-5 weeks)
3. **Themed UI skins** (C) - High user satisfaction, clear scope

### Phase 3: Complex Features (7-9 weeks)
4. **Cheat system** (A) - Most requested feature
5. **Custom controls** (D) - Requires most refactoring

### Alternative Approach: Hire/Find
- **Open-source contributions:** Post feature requests, offer bounties
- **Fork existing emulators:** Check RetroArch Android, Pizza Boy GBA (has cheats)
- **Reference implementations:**
  - Cheats: RetroArch has full cheat support
  - Themed UI: "My Boy!" GBA emulator (proprietary)
  - Custom controls: DraStic DS emulator

---

## RISKS & CHALLENGES

1. **LibretroDroid limitations:** May not expose all needed APIs (especially for cheats)
2. **Touch control refactor:** Current architecture not designed for free-form layouts
3. **Performance:** Complex shaders + overlays could impact frame rate
4. **Asset creation:** Themed UI needs high-quality graphics/textures
5. **Testing:** Controls especially need extensive testing on various devices
6. **Maintenance:** More features = more code to maintain

---

## CONCLUSION

**Feasibility: HIGH** - All features are technically possible

**Effort: SIGNIFICANT** - 3.5-5.5 months full-time

**Recommendation:**
- Start with **screen rendering** (E) and **themed UI** (C) for quick visual impact
- Add **cheat support** (A) as standalone feature
- Consider **custom controls** (D) as separate major refactor project

**VS Code Viability: POSSIBLE** but Android Studio strongly recommended for Android development, especially for Compose UI work and debugging.

Would you like me to start implementation on any specific feature?
