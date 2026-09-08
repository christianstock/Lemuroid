package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.shared.game.skins.GbcSkin

/**
 * GBC skin renderer with full-width screen bezel assembly and deep camera clearance.
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
    val bezelRect = remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                // Required for BlendMode.Clear to work
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawBehind {
                // 1. Draw solid case color
                drawRect(color = skin.caseColor)
                
                // 2. Punch a hole for the screen
                bezelRect.value?.let {
                    drawRect(
                        color = Color.Transparent,
                        topLeft = it.topLeft,
                        size = it.size,
                        blendMode = BlendMode.Clear
                    )
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // --- SECTION: CAMERA HOLE CLEARANCE MARGIN ---
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        // --- SECTION 1: DISPLAY ASSY ---
        Box(
            modifier = Modifier
                .fillMaxWidth(1.0f)
                .aspectRatio(1.1f)
                .padding(16.dp)
                .onGloballyPositioned {
                    bezelRect.value = it.boundsInParent()
                }
                .border(1.dp, Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            gameScreenContent()
        }

        // --- SECTION 2: CENTRAL DEVICE GAP ---
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )

        // --- SECTION 3: CONTROLS PAD ---
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

        // --- SECTION 4: INTERACTIVE BAR (Moved to bottom) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            interactiveBar()
        }
    }
}
