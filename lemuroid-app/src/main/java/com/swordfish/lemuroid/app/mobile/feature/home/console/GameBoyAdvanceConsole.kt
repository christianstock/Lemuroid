package com.swordfish.lemuroid.app.mobile.feature.home.console

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.shared.game.skins.GbaSkinManager

@Composable
fun GameBoyAdvanceConsole(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val caseColor = remember(context) {
        runCatching { GbaSkinManager.getInstance(context).getSelectedSkin().caseColor }
            .getOrDefault(Color(0xFF5E50A1)) // Indigo GBA
    }

    val bevelColor = remember(context) { Color(0xFF000000) }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val topOffset = 75.dp.toPx()
            val bevelOffset = topOffset + 75.dp.toPx()
            val caseHeight = 75.dp.toPx()
            val arcLift = 14.dp.toPx() // Upward arc height for signature GBA curve

            clipRect(
                left = 0f,
                top = topOffset,
                right = size.width,
                bottom = size.height
            ) {
                // Base Case Surface
                drawRect(
                    color = caseColor,
                    topLeft = Offset(0f, topOffset),
                    size = Size(size.width, caseHeight)
                )

                // Top Edge Shadows
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                        startY = topOffset,
                        endY = topOffset + 6.dp.toPx()
                    ),
                    topLeft = Offset(0f, topOffset),
                    size = Size(size.width, 6.dp.toPx())
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent),
                        startY = topOffset,
                        endY = topOffset + 16.dp.toPx()
                    ),
                    topLeft = Offset(0f, topOffset),
                    size = Size(size.width, 16.dp.toPx())
                )

                // Curved Highlight Ribbon directly above GBA Curved Bevel
                val curvedHighlightPath = Path().apply {
                    moveTo(0f, bevelOffset - 8.dp.toPx())
                    quadraticTo(
                        size.width / 2f, (bevelOffset - arcLift) - 8.dp.toPx(),
                        size.width, bevelOffset - 8.dp.toPx()
                    )
                    lineTo(size.width, bevelOffset)
                    quadraticTo(
                        size.width / 2f, bevelOffset - arcLift,
                        0f, bevelOffset
                    )
                    close()
                }
                drawPath(
                    path = curvedHighlightPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.3f)),
                        startY = bevelOffset - arcLift - 8.dp.toPx(),
                        endY = bevelOffset
                    )
                )

                // Curved Bevel Path
                val bevelPath = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(0f, bevelOffset)
                    quadraticTo(
                        size.width / 2f, bevelOffset - arcLift,
                        size.width, bevelOffset
                    )
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(path = bevelPath, color = bevelColor)

                // Curved Drop Shadow onto the Bevel
                val shadowPath = Path().apply {
                    moveTo(0f, bevelOffset)
                    quadraticTo(
                        size.width / 2f, bevelOffset - arcLift,
                        size.width, bevelOffset
                    )
                    lineTo(size.width, bevelOffset + 10.dp.toPx())
                    quadraticTo(
                        size.width / 2f, (bevelOffset - arcLift) + 10.dp.toPx(),
                        0f, bevelOffset + 10.dp.toPx()
                    )
                    close()
                }
                drawPath(
                    path = shadowPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
                        startY = bevelOffset - arcLift,
                        endY = bevelOffset + 10.dp.toPx()
                    )
                )
            }
        }

        // Interactive SYSTEM Text Label
        Text(
            text = "SYSTEM",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp,
            letterSpacing = 1.sp,
            color = Color.Black.copy(alpha = 0.12f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 101.dp, end = 28.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenSettings
                )
        )
    }
}
