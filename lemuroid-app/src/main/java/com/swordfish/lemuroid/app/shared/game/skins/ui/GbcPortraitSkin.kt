package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.swordfish.lemuroid.app.shared.game.skins.GbcSkin

/**
 * GBC skin renderer with top interactive bar and structural layout separation.
 */
@Composable
fun GbcPortraitSkin(
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
            .fillMaxSize()
            .background(color = skin.caseColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // --- SECTION 0: INTERACTIVE BAR ---
        // Keeps a clean, unweighted 56dp height zone that safely clears the camera notch area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(start = 12.dp, top = 24.dp, end = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            interactiveBar()
        }

        // --- SECTION 1: DISPLAY ASSY ---
        // FIXED: Replaced .weight() with a fixed .aspectRatio(1.1f) boundary loop.
        // This stops the emulator layout from collapsing or shrinking to 0 width/height.
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .aspectRatio(1.1f)
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawBehind {
                    drawRect(color = skin.caseColor)

                    bezelRect.value?.let { rect ->
                        drawRect(
                            color = Color.Transparent,
                            topLeft = rect.topLeft,
                            size = rect.size,
                            blendMode = BlendMode.Clear
                        )
                    }
                },
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        val position = coordinates.positionInParent()
                        bezelRect.value = Rect(
                            offset = position,
                            size = coordinates.size.toSize()
                        )
                    }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                gameScreenContent()

                BezelWithScreenCutout(
                    bezelColor = skin.caseColor,
                    cutoutRect = Rect.Zero,
                    cornerRadius = 24.dp,
                    bottomCurveDepth = 24.dp,
                    modifier = Modifier.matchParentSize()
                )
            }
        }

        // --- SECTION 2: CENTRAL DEVICE GAP ---
        // This guarantees your required separation gap between the lens and controls pad
        Spacer(modifier = Modifier.height(48.dp))

        // --- SECTION 3: CONTROLS PAD ---
        // Let the remaining lower half of the phone display space belong strictly to the controls pad
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                leftPad(Modifier.weight(1f))
                rightPad(Modifier.weight(1f))
            }
        }
    }
}
