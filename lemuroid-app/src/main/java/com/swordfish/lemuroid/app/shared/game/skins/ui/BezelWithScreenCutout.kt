package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Renders a bezel with a Game Boy-style screen cutout.
 * The cutout has rounded top corners and a bulging curved bottom edge.
 */
@Composable
fun BezelWithScreenCutout(
    bezelColor: Color,
    cutoutRect: Rect, // Maintaining parameter signature compatibility
    cornerRadius: Dp = 12.dp,
    bottomCurveDepth: Dp = 24.dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val cornerPx = cornerRadius.toPx()
        val bulgePx = bottomCurveDepth.toPx()

        // FIXED: Bleed the outer bezel mask path way past the canvas constraints (adding a 100px shield rim).
        // This completely traps and paints over any stray black background/emulator lines!
        val bleed = 100f
        val outerPath = Path().apply {
            addRect(
                Rect(
                    left = -bleed,
                    top = -bleed,
                    right = size.width + bleed,
                    bottom = size.height + bleed + bulgePx // Added bulge room to the floor cover
                )
            )
        }

        val l = 0f
        val t = -16f
        val r = size.width
        // FIXED: Restored 'b' to the absolute floor of the canvas box so your game screen stays low!
        val b = size.height

        val cutoutPath = Path().apply {
            // 1. Start at top-left
            moveTo(l + cornerPx, t)

            // 2. Top edge to top-right
            lineTo(r - cornerPx, t)
            quadraticTo(r, t, r, t + cornerPx)

            // 3. Right edge down to bottom-right
            lineTo(r, b - cornerPx)
            quadraticTo(r, b, r - cornerPx, b)

            // 4. Curved bottom edge (now safely bulges past 'b' into our expanded bleed zone!)
            quadraticTo(
                (l + r) / 2f, b + bulgePx,
                l + cornerPx, b
            )

            // 5. Bottom-left corner up to left edge
            quadraticTo(l, b, l, b - cornerPx)

            // 6. Left edge back up to top-left
            lineTo(l, t + cornerPx)
            quadraticTo(l, t, l + cornerPx, t)

            close()
        }

        // Combine: outer rect with cutout removed (EvenOdd fill type)
        val combined = Path().apply {
            fillType = PathFillType.EvenOdd
            addPath(outerPath)
            addPath(cutoutPath)
        }

        drawPath(combined, color = bezelColor)
    }
}
