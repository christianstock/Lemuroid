package com.swordfish.lemuroid.app.shared.game.skins.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object GbArt {
    fun DrawScope.drawHandheld(
        caseColor: Color,
        viewportRect: Rect?,
        isCarouselMode: Boolean
    ) {
        val w = size.width
        val h = size.height
        val corner = 16.dp.toPx()

        // 1. Draw Shell
        if (isCarouselMode) {
            val path = Path().apply {
                addRoundRect(
                    RoundRect(
                        Rect(0f, 0f, w, h),
                        topLeft = CornerRadius(corner),
                        topRight = CornerRadius(corner),
                        bottomLeft = CornerRadius.Zero,
                        bottomRight = CornerRadius.Zero
                    )
                )
            }
            drawPath(path, caseColor)
        } else {
            drawRect(caseColor)
        }

        // 2. Draw Bezel / Cutout
        viewportRect?.let { rect ->
            val standardCorner = 12.dp.toPx()
            val extraRoundCorner = 56.dp.toPx()

            val bezelPath = Path().apply {
                moveTo(rect.left + standardCorner, rect.top)
                lineTo(rect.right - standardCorner, rect.top)
                quadraticTo(rect.right, rect.top, rect.right, rect.top + standardCorner)
                lineTo(rect.right, rect.bottom - extraRoundCorner)
                quadraticTo(rect.right, rect.bottom, rect.right - extraRoundCorner, rect.bottom)
                lineTo(rect.left + standardCorner, rect.bottom)
                quadraticTo(rect.left, rect.bottom, rect.left, rect.bottom - standardCorner)
                lineTo(rect.left, rect.top + standardCorner)
                quadraticTo(rect.left, rect.top, rect.left + standardCorner, rect.top)
                close()
            }

            if (!isCarouselMode) {
                drawPath(bezelPath, Color.Transparent, blendMode = BlendMode.Clear)
            } else {
                drawPath(bezelPath, Color(0xFF1A1A1A))
            }

            // 3. Line Art / Details
            val brandingColor = Color.Black.copy(alpha = 0.4f)
            
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(
                        (brandingColor.alpha * 255).toInt(),
                        (brandingColor.red * 255).toInt(),
                        (brandingColor.green * 255).toInt(),
                        (brandingColor.blue * 255).toInt()
                    )
                    textSize = 14.sp.toPx()
                    typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD_ITALIC)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText("Nintendo GAME BOY", w / 2, rect.top - 20.dp.toPx(), paint)
            }

            if (isCarouselMode) {
                val detailColor = Color.Black.copy(alpha = 0.2f)
                val speakerX = w - 40.dp.toPx()
                val speakerY = h - 30.dp.toPx()
                repeat(5) { i ->
                    val x = speakerX + (i * 6.dp.toPx())
                    drawLine(
                        color = detailColor,
                        start = Offset(x, speakerY),
                        end = Offset(x - 10.dp.toPx(), speakerY + 20.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }
    }
}
