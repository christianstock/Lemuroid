# GBC Skin Integration Guide

## Quick Reference for Next Steps

### Phase 1: Add Skin Selection to UI

The GBC skin selection screen is ready to use. To integrate it into a settings menu:

```kotlin
// In your SettingsScreen or similar
val gbcSkinManager = GbcSkinManager(context)

GbcSkinSelectionScreen(
    skinManager = gbcSkinManager,
    onSkinSelected = { skinId ->
        gbcSkinManager.setSelectedSkin(skinId)
    }
)
```

---

### Phase 2: Render Skins in Game Screen

Currently, the game screen applies DPI-aware scaling but doesn't render the handheld skin wrapper.

**Current State** (MobileGameScreen.kt):
```kotlin
// Screen is sized with handheld scaling but appears as bare screen
Box(
    modifier = Modifier
        .size(
            width = handheldScaling.targetWidthDp.dp,
            height = handheldScaling.targetHeightDp.dp
        )
)
```

**To Render With Skin** (pseudocode - modify MobileGameScreen.kt):
```kotlin
// 1. Get current GBC skin
val gbcSkinManager = remember { GbcSkinManager(context) }
val currentSkin = gbcSkinManager.getSelectedSkinFlow().collectAsState(GbcSkin.LEMUROID_DEFAULT)

// 2. Check if game is GBC
val isGbc = GameSystem.findById(viewModel.game.systemId).id.name == "GBC"

// 3. Conditionally wrap screen
if (isGbc && handheldScaling != null) {
    if (isLandscape) {
        GbcLandscapeSkin(
            skin = currentSkin.value,
            gameScreenContent = {
                // Current game view goes here
            }
        )
    } else {
        GbcPortraitSkin(
            skin = currentSkin.value,
            gameScreenContent = {
                // Current game view goes here
            }
        )
    }
} else {
    // Render without skin (glass theme or other)
    Box { /* current rendering */ }
}
```

---

### Phase 3: Extract Game View to Composable

The game rendering logic needs to be extracted so it can be passed as a lambda to the skin rendereres.

**Current Architecture** (simplified):
```
MobileGameScreen
  ├── PadKit (input handling)
  │   ├── AndroidView (retroGameView)
  │   ├── ConstraintLayout (controls)
  │   │   ├── gameView (Box with game content)
  │   │   └── touch pads
  │   └── GameScreenRunningCentralMenu
  └── InteractiveTopBar
```

**To Support Skins**:
```
MobileGameScreen
  ├── PadKit
  │   ├── GbcPortraitSkin/GbcLandscapeSkin (wrapper)
  │   │   └── GameViewContent  ← NEW: extracted composable
  │   │       ├── AndroidView (retroGameView)
  │   │       └── ConstraintLayout (controls)
  │   └── InteractiveTopBar
```

---

### Key Utility Functions Available

```kotlin
// Get all available GBC skins
val allSkins = gbcSkinManager.getAllSkins()

// Get selected skin (reactive)
val selectedSkinFlow: Flow<GbcSkin> = gbcSkinManager.getSelectedSkinFlow()

// Change selected skin
gbcSkinManager.setSelectedSkin(skinId)

// Access skin properties
skin.id              // e.g., "berry"
skin.name            // e.g., "Berry"
skin.caseColor       // Color(0xFFC81F55)
skin.buttonsColor    // Color(0xFF1C1C1C)
skin.isDefault       // Boolean
```

---

### Skin Rendering Components

#### Portrait Mode
- **File**: `GbcPortraitSkin.kt`
- **Usage**: `GbcPortraitSkin(skin, gameScreenContent = { /* game view */ })`
- **Layout**: Top 45% screen, Bottom 55% controls
- **Parameters**: `skin: GbcSkin`, `gameScreenContent: @Composable () -> Unit`

#### Landscape Mode
- **File**: `GbcLandscapeSkin.kt`
- **Usage**: `GbcLandscapeSkin(skin, gameScreenContent = { /* game view */ })`
- **Layout**: Left 25% D-Pad, Center 50% screen, Right 25% buttons
- **Parameters**: `skin: GbcSkin`, `gameScreenContent: @Composable () -> Unit`

---

### Data Persistence

All skin selections are automatically saved to SharedPreferences:
- Prefs name: `"gbc_skin_preferences"`
- Key: `"selected_gbc_skin"`
- Survives app restart

No additional setup needed - `GbcSkinManager` handles it automatically.

---

### GameSystem Reference

To check if a game is GBC:
```kotlin
val system = GameSystem.findById(game.systemId)
val isGbc = system.id.name == "GBC"
val isGb = system.id.name == "GB"
val isGba = system.id.name == "GBA"
```

---

### Future: GB and GBA Support

The skin system is designed to be extensible:

1. Create `GbSkin.kt` with GB dimensions (47mm × 43mm)
2. Create `GbaSkin.kt` with GBA dimensions (61.2mm × 40.8mm)
3. Create portrait/landscape renderers for each
4. Use conditional logic to render appropriate skin based on `GameSystem`

All GBC code can serve as a template.

---

### Testing Checklist

- [ ] Can select different skins in menu
- [ ] Selection persists after restart
- [ ] Screen displays with correct skin colors
- [ ] Screen scaled to correct physical dimensions
- [ ] Controls visible and properly positioned
- [ ] Works in both portrait and landscape
- [ ] Non-GBC games still display normally (glass theme)

---

### Build & Deploy

Current build status: ✅ PASSING (all variants)

To rebuild after changes:
```bash
cd C:\Users\user\Documents\Projects\Lemuroid-master
.\gradlew assembleFreeDynamicDebug  # Fast iteration
.\gradlew assembleDebug              # All variants
```

Successful build takes ~6-10 seconds with gradle cache.


