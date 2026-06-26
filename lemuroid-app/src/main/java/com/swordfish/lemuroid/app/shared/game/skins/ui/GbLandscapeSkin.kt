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
import androidx.compose.ui.BiasAlignment
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
import com.swordfish.lemuroid.app.shared.game.skins.GbSkin

/**
 * GB skin renderer for landscape mode featuring widened grips, a narrow top menu,
 * and a single-cutout master window with a deeply rounded bottom-right corner.
 */
@Composable
fun GbLandscapeSkin(
    skin: GbSkin,
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
                // Necessary for BlendMode.Clear to cleanly drop out the vector shape
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawBehind {
                // 1. Fill background with solid case color
                drawRect(color = skin.caseColor)

                // 2. Punch out the single asymmetrical screen window matching the layout bounds
                bezelRect.value?.let { rect ->
                    val standardCorner = 12.dp.toPx()
                    // Replicated the amplified custom bottom-right corner radius
                    val extraRoundCorner = 56.dp.toPx()

                    val l = rect.left
                    val t = rect.top
                    val r = rect.right
                    val b = rect.bottom

                    val customCutoutPath = Path().apply {
                        moveTo(l + standardCorner, t)
                        lineTo(r - standardCorner, t)
                        quadraticTo(r, t, r, t + standardCorner)

                        // Right-side edge tracks down into the deep hook profile
                        lineTo(r, b - extraRoundCorner)
                        quadraticTo(r, b, r - extraRoundCorner, b)

                        lineTo(l + standardCorner, b)
                        quadraticTo(l, b, l, b - standardCorner)

                        lineTo(l, t + standardCorner)
                        quadraticTo(l, t, l + standardCorner, t)
                        close()
                    }

                    drawPath(
                        path = customCutoutPath,
                        color = Color.Transparent,
                        blendMode = BlendMode.Clear
                    )
                }
            }
    ) {
        // --- SECTION 0: INTERACTIVE BAR (TOP & NARROW) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(color = Color.Transparent) // Keeps the raw case color visible underneath
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
                    .weight(0.32f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                leftPad(Modifier.fillMaxSize())
            }

            // CENTER VIEWPORT HOUSING
            Box(
                modifier = Modifier
                    .weight(0.36f)
                    .fillMaxHeight()
                    .padding(top = 54.dp, bottom = 8.dp)
                    .onGloballyPositioned {
                        bezelRect.value = it.boundsInParent()
                    },
                contentAlignment = Alignment.TopCenter // Anchor everything to the top edge of the cutout
            ) {
                // FIXED: Wrapped in an inner box with targeted bottom padding
                // to physically shove the video up away from the bottom edge.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp), // Increase this value to push the video higher up
                    contentAlignment = Alignment.Center
                ) {
                    gameScreenContent()
                }
            }

            // RIGHT GRIP (ACTION BUTTONS SIDE)
            Box(
                modifier = Modifier
                    .weight(0.32f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                rightPad(Modifier.fillMaxSize())
            }
        }
    }
}
