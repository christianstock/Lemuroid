# Plan: Physical Screen Size Rendering for GB, GBC, and GBA

## Objective
Render the game view for GB, GBC, and GBA at their exact physical dimensions (mm) on the phone screen, based on the phone's DPI. If the physical screen is too small, scale down.

## Target Dimensions
| System | Physical Size (mm) | Resolution (px) | Aspect Ratio |
|--------|---------------------|-----------------|--------------|
| GameBoy | 45.5 x 41.5 | 160 x 144 | ~1.1:1 |
| GameBoy Color | 43.0 x 39.0 | 160 x 144 | ~1.1:1 |
| GameBoy Advance | 61.2 x 40.8 | 240 x 160 | 1.5:1 |

*Note: Dimensions are Width x Height.*

## Implementation Plan

### 1. Calculate Target Dimensions in Pixels
To convert physical mm to pixels:
`Pixels = (mm / 25.4) * DPI`

Use `xdpi` for width and `ydpi` for height from `DisplayMetrics`.

### 2. Scaling Logic
If `TargetPixelsWidth > AvailableWidth` or `TargetPixelsHeight > AvailableHeight`:
1. Calculate scale factors: `scaleW = AvailableWidth / TargetPixelsWidth`, `scaleH = AvailableHeight / TargetPixelsHeight`.
2. Use `min(scaleW, scaleH)` as the global scale.
3. Final Pixels = `TargetPixels * scale`.

### 3. Code Locations to Modify

#### `MobileGameScreen.kt`
- Access `LocalContext.current.resources.displayMetrics` to get `xdpi` and `ydpi`.
- Access `LocalDensity.current` if conversion to DP is needed for Compose modifiers.
- Identify the system ID using `viewModel.game.systemId`.
- Replace the `fillMaxSize` or current sizing logic for the `CONSTRAINTS_GAME_VIEW` container.

#### `GameScreenLayout.kt`
- Currently `CONSTRAINTS_GAME_VIEW` uses `Dimension.fillToConstraints`.
- You might need to change this to `Dimension.value(dp)` once dimensions are calculated, or handle sizing in the `Box` wrapper within `MobileGameScreen.kt`.

### 4. Mathematical Implementation (Pseudocode)
```kotlin
val metrics = context.resources.displayMetrics
val targetWidthMm = when(systemId) {
    "gb" -> 45.5f
    "gbc" -> 43.0f
    "gba" -> 61.2f
    else -> null
}
val targetHeightMm = when(systemId) {
    "gb" -> 41.5f
    "gbc" -> 39.0f
    "gba" -> 40.8f
    else -> null
}

if (targetWidthMm != null && targetHeightMm != null) {
    var widthPx = (targetWidthMm / 25.4f) * metrics.xdpi
    var heightPx = (targetHeightMm / 25.4f) * metrics.ydpi
    
    val scale = minOf(1f, maxWidthPx / widthPx, maxHeightPx / heightPx)
    widthPx *= scale
    heightPx *= scale
    
    // Apply widthPx and heightPx to game view
}
```

## Constraints
- Do NOT modify other systems.
- Ensure `GLRetroView` viewport is updated if the container size changes.
