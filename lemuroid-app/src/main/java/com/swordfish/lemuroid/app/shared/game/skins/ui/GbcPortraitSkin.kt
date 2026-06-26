package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
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
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // --- SECTION: CAMERA HOLE CLEARANCE MARGIN ---
        // FIXED: Increased height from 48.dp to 64.dp for extra safety clearance
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(color = skin.caseColor)
        )

        // --- SECTION 0: INTERACTIVE BAR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(color = skin.caseColor)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            interactiveBar()
        }

        // --- SECTION 1: DISPLAY ASSY ---
        Box(
            modifier = Modifier
                // FIXED: Changed from 0.95f to full bleed (1.0f) to cover the full screen width!
                .fillMaxWidth(1.0f)
                .aspectRatio(1.1f)
                .drawBehind {
                    val cornerPx = 24.dp.toPx()
                    val bulgePx = 24.dp.toPx()

                    // FIXED: Increased outer padding to 16.dp to keep the glass bezel edges
                    // proportional now that the outer container expands completely to the sides
                    val paddingPx = 16.dp.toPx()

                    val l = paddingPx
                    val t = paddingPx
                    val r = size.width - paddingPx
                    val b = size.height - paddingPx

                    val cutoutPath = Path().apply {
                        moveTo(l + cornerPx, t)
                        lineTo(r - cornerPx, t)
                        quadraticTo(r, t, r, t + cornerPx)
                        lineTo(r, b - cornerPx)
                        quadraticTo(r, b, r - cornerPx, b)
                        quadraticTo((l + r) / 2f, b + bulgePx, l + cornerPx, b)
                        quadraticTo(l, b, l, b - cornerPx)
                        lineTo(l, t + cornerPx)
                        quadraticTo(l, t, l + cornerPx, t)
                        close()
                    }

                    clipPath(path = cutoutPath, clipOp = ClipOp.Difference) {
                        drawRect(
                            color = skin.caseColor,
                            topLeft = Offset.Zero,
                            size = size
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // Matches the internal rendering padding rules above
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                gameScreenContent()
            }
        }

        // --- SECTION 2: CENTRAL DEVICE GAP ---
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(color = skin.caseColor)
        )

        // --- SECTION 3: CONTROLS PAD ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = skin.caseColor),
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
