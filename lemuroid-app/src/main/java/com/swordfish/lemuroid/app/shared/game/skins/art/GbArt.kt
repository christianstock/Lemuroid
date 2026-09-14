package com.swordfish.lemuroid.app.shared.game.skins.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
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
            // Top Details: Blue line, Red line, and Text
            val topLabelColor = Color.Black.copy(alpha = 0.6f)
            val lineBlue = Color(0xFF3639a0).copy(alpha = 0.8f)
            val lineRed = Color(0xFFa03636).copy(alpha = 0.8f)

            val lineY = rect.top - 12.dp.toPx()
            drawLine(lineBlue, Offset(rect.left + 8.dp.toPx(), lineY - 2.dp.toPx()), Offset(rect.right - 8.dp.toPx(), lineY - 2.dp.toPx()), strokeWidth = 1.dp.toPx())
            drawLine(lineRed, Offset(rect.left + 8.dp.toPx(), lineY + 2.dp.toPx()), Offset(rect.right - 8.dp.toPx(), lineY + 2.dp.toPx()), strokeWidth = 1.dp.toPx())

            drawContext.canvas.nativeCanvas.apply {
                val topPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(
                        (topLabelColor.alpha * 255).toInt(),
                        (topLabelColor.red * 255).toInt(),
                        (topLabelColor.green * 255).toInt(),
                        (topLabelColor.blue * 255).toInt()
                    )
                    textSize = 9.sp.toPx()
                    typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText("DOT MATRIX WITH STEREO SOUND", w / 2, rect.top - 18.dp.toPx(), topPaint)

                // Bottom "Nintendo GAME BOY" text
                val bottomLabelColor = Color(0xFF3639a0)
                val bottomPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(
                        255,
                        (bottomLabelColor.red * 255).toInt(),
                        (bottomLabelColor.green * 255).toInt(),
                        (bottomLabelColor.blue * 255).toInt()
                    )
                    textSize = 16.sp.toPx()
                    typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
                    textAlign = android.graphics.Paint.Align.LEFT
                }
                
                val nintendoText = "Nintendo "
                val gbText = "GAME BOY"
                val nintendoWidth = bottomPaint.measureText(nintendoText)
                val totalWidth = nintendoWidth + bottomPaint.measureText(gbText)
                
                val startX = (w - totalWidth) / 2
                drawText(nintendoText, startX, rect.bottom + 28.dp.toPx(), bottomPaint)
                
                bottomPaint.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD_ITALIC)
                drawText(gbText, startX + nintendoWidth, rect.bottom + 28.dp.toPx(), bottomPaint)
            }

            if (isCarouselMode) {
                // Background for screen in carousel mode
                val bezelPath = calculateBezelPath(rect)
                drawPath(bezelPath, Color(0xFF1A1A1A))

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

    private fun DrawScope.calculateBezelPath(rect: Rect): Path {
        val standardCorner = 12.dp.toPx()
        val extraRoundCorner = 56.dp.toPx()

        return Path().apply {
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
    }
}
