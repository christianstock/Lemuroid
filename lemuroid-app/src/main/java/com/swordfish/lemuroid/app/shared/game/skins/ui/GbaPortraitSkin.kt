package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
 * GBA skin renderer for portrait mode featuring an expanded
 * wide cutout window with a stylized bottom bow edge tracking profile.
 */
@Composable
fun GbaPortraitSkin(
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
                // 1. Paint the solid outer case shell color
                drawRect(color = skin.caseColor)

                // 2. Clear out the single master screen hole using explicit path parameters
                bezelRect.value?.let { rect ->
                    val cornerPx = 20.dp.toPx()
                    val bulgePx = 16.dp.toPx()

                    val xLeft = rect.left
                    val xRight = rect.right
                    val yTop = rect.top
                    val yBottom = rect.bottom
                    val xCenter = (xLeft + xRight) / 2f

                    // FIXED: Eliminated `.apply { }` scope block entirely.
                    // Accessing properties directly via `path.` removes type ambiguity.
                    val gbaCutoutPath = Path()

                    gbaCutoutPath.moveTo(xLeft + cornerPx, yTop)
                    gbaCutoutPath.lineTo(xRight - cornerPx, yTop)
                    gbaCutoutPath.quadraticTo(xRight, yTop, xRight, yTop + cornerPx)

                    // Right side down to baseline bounds
                    gbaCutoutPath.lineTo(xRight, yBottom - cornerPx)
                    gbaCutoutPath.quadraticTo(xRight, yBottom, xRight - cornerPx, yBottom)

                    // THE BOW: Sweeps cleanly under the display housing baseline
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
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.fillMaxWidth().height(64.dp))

        // --- SECTION 0: INTERACTIVE BAR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            interactiveBar()
        }

        // --- SECTION 1: GBA TALLER/WIDER VIEWPORT HOUSING ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.35f) // Correctly scaled for a larger, prominent GBA lens housing
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .onGloballyPositioned {
                    bezelRect.value = it.boundsInParent()
                },
            contentAlignment = Alignment.Center
        ) {
            gameScreenContent()
        }

        Spacer(modifier = Modifier.fillMaxWidth().height(48.dp))

        // --- SECTION 2: CONTROLS PAD ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            leftPad(Modifier.weight(1f))
            rightPad(Modifier.weight(1f))
        }
    }
}
