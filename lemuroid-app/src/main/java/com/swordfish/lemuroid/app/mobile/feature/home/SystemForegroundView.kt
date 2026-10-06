package com.swordfish.lemuroid.app.mobile.feature.home

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.shared.game.skins.GbSkinManager
import com.swordfish.lemuroid.app.shared.game.skins.GbaSkinManager
import com.swordfish.lemuroid.app.shared.game.skins.GbcSkinManager

@Composable
fun SystemForegroundView(
    systemId: String?,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val systemIdNorm = remember(systemId) { systemId?.lowercase().orEmpty() }

    // Safely load active skin colors with fallbacks
    val caseColor = remember(systemIdNorm, context) {
        when (systemIdNorm) {
            "gb" -> runCatching { GbSkinManager.getInstance(context).getSelectedSkin().caseColor }.getOrNull()
            "gbc" -> runCatching { GbcSkinManager.getInstance(context).getSelectedSkin().caseColor }.getOrNull()
            "gba" -> runCatching { GbaSkinManager.getInstance(context).getSelectedSkin().caseColor }.getOrNull()
            else -> null
        } ?: when (systemIdNorm) {
            "psp" -> Color(0xFF1A1A1A)
            "gb" -> Color(0xFFC4C2B8)  // Classic Game Boy DMG case color
            "gbc" -> Color(0xFF7B2CBF) // Atomic Purple / Purple GBC
            "gba" -> Color(0xFF5E50A1) // Indigo GBA
            else -> Color(0xFF444448)
        }
    }

    val bevelColor = remember(systemIdNorm, context) {
        when (systemIdNorm) {
            "gb" -> runCatching { GbSkinManager.getInstance(context).getSelectedSkin().screenLensColor }.getOrNull()
            else -> null
        } ?: Color(0xFF000000)
    }

    val isGbDmg = systemIdNorm == "gb"
    val isGba = systemIdNorm == "gba"

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val topOffset = 75.dp.toPx()
            val bevelOffset = topOffset + 75.dp.toPx()
            val caseHeight = 75.dp.toPx()
            val arcLift = 14.dp.toPx() // Upward arc height for GBA

            clipRect(
                left = 0f,
                top = topOffset,
                right = size.width,
                bottom = size.height
            ) {
                // 1. Base Case Surface
                drawRect(
                    color = caseColor,
                    topLeft = Offset(0f, topOffset),
                    size = Size(size.width, caseHeight)
                )

                // Top Edge Shadow Overlays
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

                // 2. Bottom Case Lip Highlight
                if (isGba) {
                    // Curved White Highlight Ribbon directly above GBA Bevel
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
                } else {
                    // Straight White Highlight
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.3f)),
                            startY = bevelOffset - 8.dp.toPx(),
                            endY = bevelOffset
                        ),
                        topLeft = Offset(0f, bevelOffset - 8.dp.toPx()),
                        size = Size(size.width, 8.dp.toPx())
                    )
                }

                // 3. Bevel Layer & Top Drop Shadow
                if (isGba) {
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
                } else {
                    // Straight Bevel
                    drawRect(
                        color = bevelColor,
                        topLeft = Offset(0f, bevelOffset),
                        size = Size(size.width, size.height - bevelOffset)
                    )

                    // Straight Drop Shadow onto the Bevel
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

            // DMG-01 Branding Accent Lines (For Game Boy DMG)
            if (isGbDmg) {
                val lineY = topOffset + 120.dp.toPx()
                val stroke = 7.dp.toPx()
                val textPadding = 200.dp.toPx()
                val margin = 0.dp.toPx()

                // Magenta Line
                drawLine(
                    color = Color(0xFF930551),
                    start = Offset(margin, lineY - 7.dp.toPx()),
                    end = Offset(size.width - textPadding, lineY - 7.dp.toPx()),
                    strokeWidth = stroke
                )
                // Blue Line
                drawLine(
                    color = Color(0xFF111B91),
                    start = Offset(margin, lineY + 7.dp.toPx()),
                    end = Offset(size.width - textPadding, lineY + 7.dp.toPx()),
                    strokeWidth = stroke
                )
            }
        }

        // DMG-01 Branding Text Overlay
        if (isGbDmg) {
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
