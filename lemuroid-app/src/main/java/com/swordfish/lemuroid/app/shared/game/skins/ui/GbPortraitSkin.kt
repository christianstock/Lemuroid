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
import com.swordfish.lemuroid.app.shared.game.skins.GbSkin

/**
 * GB skin renderer for portrait mode with asymmetrical master cutout framing.
 */
@Composable
fun GbPortraitSkin(
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
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawBehind {
                // 1. Paint the solid outer case shell color
                drawRect(color = skin.caseColor)

                // 2. Clear out the single master screen hole using the asymmetric vector path
                bezelRect.value?.let { rect ->
                    val standardCorner = 12.dp.toPx()
                    // FIXED: Increased radius to make the asymmetrical curve noticeably bigger
                    val extraRoundCorner = 56.dp.toPx()

                    val l = rect.left
                    val t = rect.top
                    val r = rect.right
                    val b = rect.bottom

                    val customCutoutPath = Path().apply {
                        moveTo(l + standardCorner, t)
                        lineTo(r - standardCorner, t)
                        quadraticTo(r, t, r, t + standardCorner)

                        // Right-side line sweeps down into the deepened bottom-right profile hook
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
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.fillMaxWidth().height(64.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            interactiveBar()
        }

        // --- SECTION 1: MASTER DISPLAY VIEWPORT CONTAINER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.1f)
                .padding(16.dp)
                .onGloballyPositioned {
                    bezelRect.value = it.boundsInParent()
                },
            contentAlignment = Alignment.Center
        ) {
            // FIXED: Keep the composable context call clean here, away from canvas blocks
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
