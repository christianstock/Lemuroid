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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.mobile.shared.GameBoyBlue
import com.swordfish.lemuroid.app.mobile.shared.GameBoyMagenta
import com.swordfish.lemuroid.app.shared.game.skins.GbSkinManager

@Composable
fun GameBoyConsole(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val activeSkin = remember(context) {
        runCatching { GbSkinManager.getInstance(context).getSelectedSkin() }.getOrNull()
    }

    val caseColor = activeSkin?.caseColor ?: Color(0xFFDBD3CD)
    val bevelColor = activeSkin?.screenLensColor ?: Color(0xFF656574)

    val skinId = remember(activeSkin) { activeSkin?.id?.lowercase().orEmpty() }
    val skinName = remember(activeSkin) { activeSkin?.name?.lowercase().orEmpty() }

    val isPocket = remember(skinId, skinName) {
        skinId.contains("pocket") || skinName.contains("pocket")
    }
    val isLight = remember(skinId, skinName) {
        skinId.contains("light") || skinName.contains("light")
    }
    val isDmg = !isPocket && !isLight

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
                // Base Case Body
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

                // Screen Lens Bevel Layer
                drawRect(
                    color = bevelColor,
                    topLeft = Offset(0f, bevelOffset),
                    size = Size(size.width, size.height - bevelOffset)
                )

                // Bevel Drop Shadow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent),
                        startY = bevelOffset,
                        endY = bevelOffset + 8.dp.toPx()
                    ),
                    topLeft = Offset(0f, bevelOffset),
                    size = Size(size.width, 8.dp.toPx())
                )

                // DMG-01 Branding Lines (Magenta & Blue) — Only drawn for classic DMG
                if (isDmg) {
                    val lineY = topOffset + 120.dp.toPx()
                    val stroke = 7.dp.toPx()
                    val textPadding = 200.dp.toPx()

                    drawLine(
                        color = GameBoyMagenta,
                        start = Offset(0f, lineY - 7.dp.toPx()),
                        end = Offset(size.width - textPadding, lineY - 7.dp.toPx()),
                        strokeWidth = stroke
                    )
                    drawLine(
                        color = GameBoyBlue,
                        start = Offset(0f, lineY + 7.dp.toPx()),
                        end = Offset(size.width - textPadding, lineY + 7.dp.toPx()),
                        strokeWidth = stroke
                    )
                }
            }
        }

        // DMG-01 Screen Frame Subtext — Only displayed for original DMG
        if (isDmg) {
            Text(
                text = "DOT MATRIX WITH STEREO SOUND",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 180.dp, end = 0.dp)
                    .graphicsLayer {
                        translationX = 180.dp.toPx()
                    }
            )
        }

        // Interactive Settings Button
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
