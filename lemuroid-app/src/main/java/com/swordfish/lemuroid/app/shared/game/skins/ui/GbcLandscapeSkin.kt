package com.swordfish.lemuroid.app.shared.game.skins.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.app.shared.game.skins.GbcSkin

/**
 * GBC skin renderer for landscape mode with widened grips, narrow top menu,
 * and carefully isolated case color margins that preserve the native game surface layer.
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
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // --- SECTION 0: INTERACTIVE BAR (TOP & NARROW) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(color = skin.caseColor)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            interactiveBar()
        }

        // --- SECTION 1: MAIN LANDSCAPE ROW ---
        // Kept transparent so it doesn't mask the background game window surface
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // LEFT GRIP (D-PAD SIDE)
            Box(
                modifier = Modifier
                    .weight(0.32f)
                    .fillMaxHeight()
                    .background(color = skin.caseColor),
                contentAlignment = Alignment.Center
            ) {
                leftPad(Modifier.fillMaxSize())
            }

            // CENTER VIEWPORT HOUSING
            // CENTER VIEWPORT HOUSING
            Box(
                modifier = Modifier
                    .weight(0.36f)
                    .fillMaxHeight()
                    .drawBehind {
                        val topBorderThickness = 32.dp.toPx()
                        val bottomBorderThickness = 32.dp.toPx()

                        // 1. Draw the solid top background border strip
                        drawRect(
                            color = skin.caseColor,
                            topLeft = Offset(0f, 0f),
                            size = Size(size.width, topBorderThickness)
                        )

                        // 2. Draw the solid bottom background border strip
                        drawRect(
                            color = skin.caseColor,
                            topLeft = Offset(0f, size.height - bottomBorderThickness),
                            size = Size(size.width, bottomBorderThickness)
                        )

                        // Outer Frame Boundaries for the Dark Bezel Lens
                        val cornerPx = 24.dp.toPx()
                        val bulgePx = 20.dp.toPx() // The physical depth of the bottom bow
                        val l = 0f
                        val t = topBorderThickness
                        val r = size.width
                        val b = size.height - bottomBorderThickness

                        // 3. Build the Outer Bezel Lens Path (This includes the bottom bow!)
                        val outerBezelPath = Path().apply {
                            moveTo(l + cornerPx, t)
                            lineTo(r - cornerPx, t)
                            quadraticTo(r, t, r, t + cornerPx)
                            lineTo(r, b - cornerPx)
                            quadraticTo(r, b, r - cornerPx, b)

                            // THE BOW: Sweeps down beautifully into the bottom casing strip
                            quadraticTo((l + r) / 2f, b + bulgePx, l + cornerPx, b)

                            quadraticTo(l, b, l, b - cornerPx)
                            lineTo(l, t + cornerPx)
                            quadraticTo(l, t, l + cornerPx, t)
                            close()
                        }

                        // 4. Paint the side housing blocks around the lens frame first
                        clipPath(path = outerBezelPath, clipOp = ClipOp.Difference) {
                            drawRect(
                                color = skin.caseColor,
                                topLeft = Offset(0f, topBorderThickness),
                                size = Size(size.width, size.height - topBorderThickness - bottomBorderThickness + bulgePx)
                            )
                        }

                        // 5. Draw the Dark Bezel Glass Frame layer onto the canvas
                        // (Change Color.DarkGray to match your exact bezel tint if needed)
                        drawPath(
                            path = outerBezelPath,
                            color = Color(0xFF000000)
                        )

                        // 6. Build the Inner Screen Hole (The actual 1:1 window where the game lives)
                        // We inset this path slightly from the outer bezel lines to create the border frame
                        val insetPx = 16.dp.toPx()
                        val screenPath = Path().apply {
                            addRect(
                                Rect(
                                    left = l + insetPx,
                                    top = t + insetPx,
                                    right = r - insetPx,
                                    bottom = b - insetPx
                                )
                            )
                        }

                        // 7. PUNCH A HOLE straight through the dark bezel so the game engine shows through
                        clipPath(path = screenPath, clipOp = ClipOp.Intersect) {
                            drawRect(
                                color = Color.Transparent,
                                topLeft = Offset.Zero,
                                size = size,
                                blendMode = BlendMode.Clear // Cleanly clears out the bezel paint layer
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    gameScreenContent()
                }
            }

            // RIGHT GRIP (ACTION BUTTONS SIDE)
            Box(
                modifier = Modifier
                    .weight(0.32f)
                    .fillMaxHeight()
                    .background(color = skin.caseColor),
                contentAlignment = Alignment.Center
            ) {
                rightPad(Modifier.fillMaxSize())
            }
        }
    }
}
