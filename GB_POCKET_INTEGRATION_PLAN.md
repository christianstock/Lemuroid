# Game Boy Pocket Integration Blueprint

This document outlines the architectural changes required to add the Game Boy Pocket (GBP) as a first-class variant of the Game Boy (GB) system in Lemuroid.

---

## 1. Data Model Strategy (b and c)

To make the GBP appear next to other GB skins, we need to upgrade `GbSkin` to handle model-specific logic rather than just colors.

### Modify `GbSkin.kt`
Add a `model` property to the `GbSkin` class to distinguish between the original DMG and the Pocket.

```kotlin
enum class GbModel { DMG, POCKET }

data class GbSkin(
    val id: String,
    val name: String,
    val caseColor: Color,
    val buttonsColor: Color,
    val model: GbModel = GbModel.DMG // Default to original
)
```

### Register the Pocket Skin
In the `companion object` of `GbSkin`, add your new Pocket variants:

```kotlin
val POCKET_SILVER = GbSkin(
    id = "gbp_silver",
    name = "Game Boy Pocket (Silver)",
    caseColor = Color(0xFFC0C0C0),
    buttonsColor = Color(0xFF222222),
    model = GbModel.POCKET
)
```

---

## 2. Conditional Rendering in `GbArt.kt`

Since `GbArt.drawHandheld` is responsible for the actual drawing on the `Canvas`, this is where the geometric changes (non-slanted buttons, labels) happen.

### Update `drawHandheld` Signature
Pass the model type into the drawing function:

```kotlin
fun DrawScope.drawHandheld(
    caseColor: Color,
    bezelRect: Rect?,
    model: GbModel, // Add this
    isCarousel: Boolean = false
) {
    // ... logic for drawing shell ...

    // b) Slanted vs Vertical Start/Select
    if (model == GbModel.POCKET) {
        drawVerticalButtons(...) 
    } else {
        drawSlantedButtons(...)
    }

    // c) Cosmetic writing
    if (model == GbModel.POCKET) {
        drawText("Game Boy POCKET", ...)
    } else {
        drawText("Nintendo Game Boy", ...)
    }
}
```

---

## 3. Physical Screen Sizing (a)

The `PhysicalScreenSizeCalculator` currently uses the `SystemID` (which is just `GB`) to determine size. Since the Pocket has a larger physical screen (65mm diagonal vs 57mm), we need to pass the "Model" info down.

### Option A: Virtual System ID
Add a "dummy" system ID specifically for calculation:
```kotlin
// In SystemID.kt
object SystemID {
    val GB = "gb"
    val GBP = "gb_pocket" // Use this only for sizing calculations
}

// In PhysicalScreenSizeCalculator.kt
private val HANDHELD_DIMENSIONS = mapOf(
    SystemID.GB to PhysicalDimensions(widthMm = 47.0f, heightMm = 42.0f),
    SystemID.GBP to PhysicalDimensions(widthMm = 52.0f, heightMm = 47.0f) // Example Pocket dims
)
```

### Option B: The "Passing Problem"
In `MobileGameScreen.kt`, the calculation happens inside the `BoxWithConstraints`. You can simply check the current skin:

```kotlin
val currentSkin = gbSkinManager.getSelectedSkin()
val calculationSystemId = if (currentSkin.model == GbModel.POCKET) SystemID.GBP else SystemID.GB

val screenDims = PhysicalScreenSizeCalculator.calculateScreenDimensions(
    systemId = calculationSystemId, 
    ...
)
```

---

## 4. Scaling in the Carousel

To fix the text scaling issue and provide a uniform 1:1 scale-down, use the Compose `drawContext.canvas.scale` or a root `Modifier.graphicsLayer`.

### Carousel Scaling Fix
In `HomeCarousel.kt`, inside the `SystemForegroundView`, wrap the drawing logic in a scale block:

```kotlin
Canvas(modifier = modifier) {
    // Calculate a uniform scale factor (e.g., 0.8f)
    val scaleFactor = 0.8f
    
    withTransform({
        scale(scaleFactor, pivot = center)
    }) {
        // All drawing commands inside here (including text) 
        // will now be scaled 1:1 perfectly.
        GbArt.run { drawHandheld(...) }
    }
}
```

By using `withTransform { scale(...) }`, the text rendering engines inside `GbArt` will treat the coordinate system as smaller, resulting in perfectly sharp, scaled-down labels without needing to manually adjust font sizes.

---

## Summary of Files to Touch:
1.  **`GbSkin.kt`**: Add `GbModel` enum and new Pocket skin instances.
2.  **`GbArt.kt`**: Add `model` param to `drawHandheld` and use `if/else` for button geometry and text.
3.  **`PhysicalScreenSizeCalculator.kt`**: Add dimensions for the Pocket.
4.  **`MobileGameScreen.kt`**: Pass the correct "virtual" ID to the calculator based on the skin model.
5.  **`HomeCarousel.kt`**: Wrap the `Canvas` draw calls in a `scale` transform.
