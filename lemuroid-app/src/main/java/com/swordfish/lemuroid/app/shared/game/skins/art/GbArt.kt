package com.swordfish.lemuroid.app.shared.game.skins.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.shared.game.skins.GbSkin

object GbArt {
    fun DrawScope.drawHandheld(
        gameScreenRect: Rect?,
        bezelRect: Rect?,
        skin: GbSkin,
        isCarouselMode: Boolean
    ) {
        val width = size.width
        val height = size.height

        drawConsoleBackground(skin.caseColor, width, height, isCarouselMode)

        bezelRect?.let { rect ->
            drawScreenLens(rect, gameScreenRect, skin.screenLensColor, width, height, isCarouselMode)

            drawTopBezelBranding(drawContext.canvas.nativeCanvas, rect, gameScreenRect, skin)

            drawNintendoBrandingLabel(
                drawContext.canvas.nativeCanvas,
                startX = rect.left,
                baselineY = rect.bottom + 28.dp.toPx(),
                skin.labelColor
            )

            if (!isCarouselMode) {
                drawSpeakerGrill(width, height)
            }
        }
    }

    private fun DrawScope.drawConsoleBackground(caseColor: Color, w: Float, h: Float, isCarouselMode: Boolean) {
        if (isCarouselMode) {
            val corner = 8.dp.toPx()
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
    }

    private fun DrawScope.drawScreenLens(
        bezelRect: Rect,
        gameScreenRect: Rect?,
        lensColor: Color,
        w: Float,
        h: Float,
        isCarouselMode: Boolean
    ) {
        val bezelPath = calculateBezelPath(bezelRect)
        drawPath(bezelPath, lensColor)

        if (gameScreenRect != null) {
            if (isCarouselMode) {
                drawRect(
                    color = Color.Black,
                    topLeft = gameScreenRect.topLeft,
                    size = gameScreenRect.size
                )
            } else {
                drawRect(
                    color = Color.Transparent,
                    topLeft = gameScreenRect.topLeft,
                    size = gameScreenRect.size,
                    blendMode = BlendMode.Clear
                )
            }
        }
    }

    private fun DrawScope.drawTopBezelBranding(
        canvas: android.graphics.Canvas,
        bezelRect: Rect,
        gameScreenRect: Rect?,
        skin: GbSkin
    ) {
        val fontColor = Color.White.copy(alpha = 0.6f)
        val textPaint = android.graphics.Paint().apply {
            color = fontColor.toArgb()
            textSize = 9.sp.toPx()
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.RIGHT
        }

        val labelText = "DOT MATRIX WITH STEREO SOUND"
        val textWidth = textPaint.measureText(labelText)
        val textPadding = 12.dp.toPx()

        val screenRight = gameScreenRect?.right ?: (bezelRect.right - 24.dp.toPx())
        val leftLineEnd = screenRight - textWidth - textPadding

        val textY = bezelRect.top + 25.dp.toPx()
        val lineY = bezelRect.top + 22.dp.toPx()

        val stroke = 3.dp.toPx()
        val lineMargin = 8.dp.toPx()

        drawLine(
            skin.lineRed,
            Offset(bezelRect.left + lineMargin, lineY - 3.dp.toPx()),
            Offset(leftLineEnd, lineY - 3.dp.toPx()),
            stroke
        )
        drawLine(
            skin.lineBlue,
            Offset(bezelRect.left + lineMargin, lineY + 3.dp.toPx()),
            Offset(leftLineEnd, lineY + 3.dp.toPx()),
            stroke
        )

        canvas.drawText(labelText, screenRight, textY, textPaint)

        val rightLineStart = screenRight + textPadding
        drawLine(
            skin.lineRed,
            Offset(rightLineStart, lineY - 3.dp.toPx()),
            Offset(bezelRect.right - lineMargin, lineY - 3.dp.toPx()),
            stroke
        )
        drawLine(
            skin.lineBlue,
            Offset(rightLineStart, lineY + 3.dp.toPx()),
            Offset(bezelRect.right - lineMargin, lineY + 3.dp.toPx()),
            stroke
        )
    }

    private fun DrawScope.drawNintendoBrandingLabel(
        canvas: android.graphics.Canvas,
        startX: Float,
        baselineY: Float,
        fontColor: Color,
    ) {
        val nintendoText = "Nintendo "
        val gbText = "GAME BOY"
        val tmText = "TM"

        val paint = android.graphics.Paint().apply {
            color = fontColor.toArgb()
        }

        paint.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
        paint.textSize = 18.dp.toPx()
        val nintendoWidth = paint.measureText(nintendoText)
        canvas.drawText(nintendoText, startX, baselineY, paint)

        paint.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD_ITALIC)
        paint.textSize = 26.dp.toPx()
        val gbWidth = paint.measureText(gbText)
        canvas.drawText(gbText, startX + nintendoWidth, baselineY, paint)

        paint.typeface = android.graphics.Typeface.DEFAULT
        paint.textSize = 18.dp.toPx()
        canvas.drawText(tmText, startX + nintendoWidth + gbWidth, baselineY, paint)
    }
}

private fun DrawScope.drawSpeakerGrill(w: Float, h: Float) {
    val detailColor = Color.Black.copy(alpha = 0.2f)
    val speakerX = w - 40.dp.toPx()
    val speakerY = h - 80.dp.toPx()

    repeat(5) { i ->
        val x = speakerX - (i * 15.dp.toPx())
        drawLine(
            color = detailColor,
            start = Offset(x, speakerY),
            end = Offset(x - 50.dp.toPx(), speakerY - 100.dp.toPx()),
            strokeWidth = 10.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.calculateBezelPath(rect: Rect): Path {
    val standardCorner = 16.dp.toPx()
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

