# Plan: GBC Skin Integration and Menu Relocation

## Objective
Move GBC skin selection from the main settings to the Game Menu (only for GBC games), ensure persistence, and verify the correct application of vector skins (bezels/buttons).

## Tasks

### 1. Relocate Skin Selection to Game Menu
- **Remove** from `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/settings/general/SettingsScreen.kt` (specifically `DisplaySettings()` function).
- **Add** to `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/gamemenu/GameMenuHomeScreen.kt`.
- **Condition**: Only show the "Skins" button if `gameMenuRequest.game.systemId == "gbc"`.
- **Navigation**:
    1. Add `SKINS` to `GameMenuRoute` enum in `GameMenuNavigationRoutes.kt`.
    2. Register the route in `GameMenuActivity.kt`'s `NavHost`.
    3. Use `GbcSkinSelectionScreen.kt` for the content.

### 2. Verify and Apply Skins in Game Screen
- **Persistence**: `GbcSkinManager` already uses `SharedPreferences` for persistence. No changes needed.
- **Application**:
    - Modify `lemuroid-app/src/main/java/com/swordfish/lemuroid/app/mobile/feature/game/MobileGameScreen.kt`.
    - Check for `isGbc` and if a skin is selected.
    - Use `GbcPortraitSkin.kt` and `GbcLandscapeSkin.kt` to wrap the game rendering content.
    - **Current Issue**: The current skins are simple Compose boxes/columns. They are NOT full vector/image bezels yet. You may need to refine the `GbcPortraitSkin` to look more authentic or use SVG assets if provided.

### 3. Verification Checklist
- [ ] Skin selection only visible in Game Menu when playing GBC.
- [ ] Selection survives app restart.
- [ ] Skin color (Case/Buttons) reflects choice in-game.
- [ ] Game view is correctly centered within the skin bezel.

## Files Involved
- `GameMenuNavigationRoutes.kt` (New Route)
- `GameMenuActivity.kt` (Route Registration)
- `GameMenuHomeScreen.kt` (Add Menu Entry)
- `MobileGameScreen.kt` (Wrap Game View)
- `SettingsScreen.kt` (Cleanup)
- `GbcPortraitSkin.kt` / `GbcLandscapeSkin.kt` (Verification/Refinement)
