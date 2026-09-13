package com.swordfish.lemuroid.app.shared.game.skins.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object PspArt {
    fun DrawScope.drawHandheld(
        caseColor: Color,
        viewportRect: Rect?,
        isCarouselMode: Boolean
    ) {
        val w = size.width
        val h = size.height
        val corner = 32.dp.toPx()

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
            val bezelPath = Path().apply {
                addRoundRect(RoundRect(rect, CornerRadius(16.dp.toPx())))
            }

            if (!isCarouselMode) {
                drawPath(bezelPath, Color.Transparent, blendMode = BlendMode.Clear)
            } else {
                drawPath(bezelPath, Color(0xFF1A1A1A))
            }

            // 3. Line Art / Details
            val brandingColor = Color.White.copy(alpha = 0.4f)
            
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(
                        (brandingColor.alpha * 255).toInt(),
                        (brandingColor.red * 255).toInt(),
                        (brandingColor.green * 255).toInt(),
                        (brandingColor.blue * 255).toInt()
                    )
                    textSize = 14.sp.toPx()
                    typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText("PSP", w / 2, rect.top - 20.dp.toPx(), paint)
            }

            if (isCarouselMode) {
                val shoulderW = w * 0.25f
                val shoulderH = 12.dp.toPx()
                drawRect(color = Color.White.copy(alpha = 0.1f), topLeft = Offset(0f, 0f), size = Size(shoulderW, shoulderH))
                drawRect(color = Color.White.copy(alpha = 0.1f), topLeft = Offset(w - shoulderW, 0f), size = Size(shoulderW, shoulderH))
            }
        }
    }
}
