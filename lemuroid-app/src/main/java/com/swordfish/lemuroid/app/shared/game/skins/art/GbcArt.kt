package com.swordfish.lemuroid.app.shared.game.skins.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object GbcArt {
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
            if (viewportRect != null) {
                val bezelPath = calculateBezelPath(viewportRect)
                val shellPath = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(Rect(0f, 0f, w, h))
                    addPath(bezelPath)
                }
                drawPath(shellPath, caseColor)
            } else {
                drawRect(caseColor)
            }
        }

        // 2. Bezel / Details
        viewportRect?.let { rect ->
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
                drawText("Nintendo GAME BOY COLOR", w / 2, rect.top - 20.dp.toPx(), paint)
            }

            if (isCarouselMode) {
                val bezelPath = calculateBezelPath(rect)
                drawPath(bezelPath, Color(0xFF1A1A1A))

                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    topLeft = Offset(w * 0.75f, 12.dp.toPx()),
                    size = Size(40.dp.toPx(), 12.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }
    }

    private fun DrawScope.calculateBezelPath(rect: Rect): Path {
        val cornerPx = 20.dp.toPx()
        val bulgePx = 20.dp.toPx()

        return Path().apply {
            moveTo(rect.left + cornerPx, rect.top)
            lineTo(rect.right - cornerPx, rect.top)
            quadraticTo(rect.right, rect.top, rect.right, rect.top + cornerPx)
            lineTo(rect.right, rect.bottom - cornerPx)
            quadraticTo(rect.right, rect.bottom, rect.right - cornerPx, rect.bottom)
            quadraticTo(rect.center.x, rect.bottom + bulgePx, rect.left + cornerPx, rect.bottom)
            quadraticTo(rect.left, rect.bottom, rect.left, rect.bottom - cornerPx)
            lineTo(rect.left, rect.top + cornerPx)
            quadraticTo(rect.left, rect.top, rect.left + cornerPx, rect.top)
            close()
        }
    }
}
