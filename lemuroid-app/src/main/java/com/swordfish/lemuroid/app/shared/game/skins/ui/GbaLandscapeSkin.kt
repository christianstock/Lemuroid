package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.shared.game.skins.GbaSkin

/**
 * GBA skin renderer for landscape mode with a narrow top menu bar,
 * an expanded wide/tall display viewport, and a single-cutout master bow frame.
 */
@Composable
fun GbaLandscapeSkin(
    skin: GbaSkin,
    gameScreenContent: @Composable () -> Unit,
    leftPad: @Composable (Modifier) -> Unit,
    rightPad: @Composable (Modifier) -> Unit,
    interactiveBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bezelRect = remember { mutableStateOf<Rect?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawBehind {
                // 1. Fill background with solid case color
                drawRect(color = skin.caseColor)

                // 2. Punch out the single asymmetrical screen window matching layout bounds
                bezelRect.value?.let { rect ->
                    val cornerPx = 20.dp.toPx()
                    val bulgePx = 16.dp.toPx()

                    val xLeft = rect.left
                    val xRight = rect.right
                    val yTop = rect.top
                    val yBottom = rect.bottom
                    val xCenter = (xLeft + xRight) / 2f

                    val gbaCutoutPath = Path()

                    gbaCutoutPath.moveTo(xLeft + cornerPx, yTop)
                    gbaCutoutPath.lineTo(xRight - cornerPx, yTop)
                    gbaCutoutPath.quadraticTo(xRight, yTop, xRight, yTop + cornerPx)

                    // Right side edge down to baseline bounds
                    gbaCutoutPath.lineTo(xRight, yBottom - cornerPx)
                    gbaCutoutPath.quadraticTo(xRight, yBottom, xRight - cornerPx, yBottom)

                    // THE BOW: Sweeps beautifully across the bottom landscape edge
                    gbaCutoutPath.quadraticTo(xCenter, yBottom + bulgePx, xLeft + cornerPx, yBottom)

                    // Left side back up to the starting anchor
                    gbaCutoutPath.quadraticTo(xLeft, yBottom, xLeft, yBottom - cornerPx)
                    gbaCutoutPath.lineTo(xLeft, yTop + cornerPx)
                    gbaCutoutPath.quadraticTo(xLeft, yTop, xLeft + cornerPx, yTop)
                    gbaCutoutPath.close()

                    drawPath(
                        path = gbaCutoutPath,
                        color = Color.Transparent,
                        blendMode = BlendMode.Clear
                    )
                }
            }
    ) {
        // --- SECTION 0: INTERACTIVE BAR (TOP & NARROW STRIP) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(color = Color.Transparent)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            interactiveBar()
        }

        // --- SECTION 1: MAIN LANDSCAPE ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // LEFT GRIP (D-PAD SIDE)
            Box(
                modifier = Modifier
                    .weight(0.25f) // Adjusted to give the center layout its new boundaries
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                leftPad(Modifier.fillMaxSize())
            }

            // CENTER VIEWPORT HOUSING
            Box(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight()
                    .padding(top = 42.dp, bottom = 8.dp)
                    .onGloballyPositioned {
                        bezelRect.value = it.boundsInParent()
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                // Inner layout shell that forces the internal video stream upwards
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // FIXED: Raised to 32.dp to aggressively shove the video layout
                        // straight up toward the top edge of the master cutout window
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.TopCenter // FIXED: Direct top anchor
                ) {
                    gameScreenContent()
                }
            }

            // RIGHT GRIP (ACTION BUTTONS SIDE)
            Box(
                modifier = Modifier
                    .weight(0.25f) // Balanced symmetrically with the left side
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                rightPad(Modifier.fillMaxSize())
            }
        }
    }
}
