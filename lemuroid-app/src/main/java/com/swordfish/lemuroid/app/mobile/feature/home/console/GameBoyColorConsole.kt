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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.shared.game.skins.GbcSkinManager

@Composable
fun GameBoyColorConsole(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val caseColor = remember(context) {
        runCatching { GbcSkinManager.getInstance(context).getSelectedSkin().caseColor }
            .getOrDefault(Color(0xFF7B2CBF)) // Atomic Purple / Purple GBC
    }

    val bevelColor = remember(context) { Color(0xFF000000) }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val topOffset = 75.dp.toPx()
            val bevelOffset = topOffset + 75.dp.toPx()
            val caseHeight = 75.dp.toPx()

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

                // Straight Lip Highlight
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.3f)),
                        startY = bevelOffset - 8.dp.toPx(),
                        endY = bevelOffset
                    ),
                    topLeft = Offset(0f, bevelOffset - 8.dp.toPx()),
                    size = Size(size.width, 8.dp.toPx())
                )

                // Bevel Layer & Top Drop Shadow
                drawRect(
                    color = bevelColor,
                    topLeft = Offset(0f, bevelOffset),
                    size = Size(size.width, size.height - bevelOffset)
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent),
                        startY = bevelOffset,
                        endY = bevelOffset + 8.dp.toPx()
                    ),
                    topLeft = Offset(0f, bevelOffset),
                    size = Size(size.width, 8.dp.toPx())
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
