package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.shared.game.skins.GbcSkin

/**
 * GBC skin renderer for landscape mode (16:9 or 19.5:9 horizontal viewport)
 *
 * Layout structure:
 * - Left 25% (Width): Left Controller Grip Panel (D-Pad)
 * - Center 50% (Width): Centered Active Game Screen Viewport (1:1 square)
 * - Right 25% (Width): Right Controller Grip Panel (Action Buttons)
 * - Everything shifted down by ~10% from top
 */
@Composable
fun GbcLandscapeSkin(
    skin: GbcSkin,
    gameScreenContent: @Composable () -> Unit,
    leftPad: @Composable (Modifier) -> Unit,
    rightPad: @Composable (Modifier) -> Unit,
    interactiveBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bezelRect = remember { mutableStateOf<Rect?>(null) }

    Column(
        modifier = modifier
            .graphicsLayer {
                // Required for BlendMode.Clear to work
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawBehind {
                // 1. Draw solid case color
                drawRect(color = skin.caseColor)
                
                // 2. Punch a hole for the screen using Game Boy style cutout
                bezelRect.value?.let {
                    drawRect(
                        color = Color.Transparent,
                        topLeft = it.topLeft,
                        size = it.size,
                        blendMode = BlendMode.Clear
                    )
                }
            }
    ) {
        // Top padding: 10% of container height
        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(0.10f))

        // Main content area with padding
        Row(
            modifier = Modifier
                .weight(0.90f)
        ) {
            // Left Grip (D-Pad side)
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                leftPad(Modifier.fillMaxSize())
            }

            // Center Screen (1:1 square)
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
                    .onGloballyPositioned {
                        bezelRect.value = it.boundsInParent()
                    },
                contentAlignment = Alignment.Center
            ) {
                gameScreenContent()

                // Render the Game Boy style bezel cutout on top
                bezelRect.value?.let {
                    BezelWithScreenCutout(
                        bezelColor = skin.caseColor,
                        cutoutRect = it,
                        cornerRadius = 6.dp,
                        bottomCurveDepth = 12.dp,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            // Right Grip (Action Buttons side)
            Box(
                modifier = Modifier
                    .weight(0.25f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                rightPad(Modifier.fillMaxSize())
            }
        }
        
        // Interactive Bar at the bottom
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)) {
            interactiveBar()
        }
    }
}



