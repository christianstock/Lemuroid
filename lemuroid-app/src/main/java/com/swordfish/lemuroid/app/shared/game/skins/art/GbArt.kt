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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.shared.game.skins.GbModel
import com.swordfish.lemuroid.app.shared.game.skins.GbSkin
import androidx.core.graphics.toColorInt

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

            if (skin.model == GbModel.DMG) {
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

            if (skin.model == GbModel.POCKET) {
                drawPocketBezelBranding(
                    canvas = drawContext.canvas.nativeCanvas,
                    bezelRect = rect,
                    gameScreenRect = gameScreenRect,
                    fontColor = skin.labelColor,
                    lensColor = skin.screenLensColor
                )

                drawPocketNintendoBranding(
                    canvas = drawContext.canvas.nativeCanvas,
                    bezelRect = rect
                )

                if (!isCarouselMode) {
                    drawPocketSpeakerGrill(width, height)
                }
            }

            if (skin.model == GbModel.LIGHT) {
            drawLightBezelBranding(
                canvas = drawContext.canvas.nativeCanvas,
                bezelRect = rect,
                gameScreenRect = gameScreenRect,
                fontColor = skin.labelColor,
                lensColor = skin.screenLensColor
            )

            drawPocketNintendoBranding(
                canvas = drawContext.canvas.nativeCanvas,
                bezelRect = rect
            )

            if (!isCarouselMode) {
                drawPocketSpeakerGrill(width, height)
            }
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
            textSize = 12.sp.toPx()
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

        paint.typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
        paint.textSize = 22.dp.toPx()
        val nintendoWidth = paint.measureText(nintendoText)
        canvas.drawText(nintendoText, startX, baselineY, paint)

        paint.typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD_ITALIC)
        paint.textSize = 32.dp.toPx()
        val gbWidth = paint.measureText(gbText)
        canvas.drawText(gbText, startX + nintendoWidth, baselineY, paint)

        paint.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
        paint.textSize = 12.dp.toPx()
        canvas.drawText(tmText, startX + nintendoWidth + gbWidth, baselineY, paint)
    }
}

private fun DrawScope.drawPocketBezelBranding(
    canvas: android.graphics.Canvas,
    bezelRect: Rect,
    gameScreenRect: Rect?,
    fontColor: Color,
    lensColor: Color,
) {
    val gbText = "GAME BOY "
    val pocketText = "pocket"

    val startX = gameScreenRect?.left ?: (bezelRect.left + 16.dp.toPx())
    val baselineY = (gameScreenRect?.bottom ?: (bezelRect.bottom - 40.dp.toPx())) + 24.dp.toPx()

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
    }

    // 1. Draw "GAME BOY"
    paint.color = fontColor.toArgb()
    paint.typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD_ITALIC)
    paint.textSize = 22.dp.toPx()

    val gbWidth = paint.measureText(gbText)
    canvas.drawText(gbText, startX, baselineY, paint)

    // 2. Measure "pocket" text for the badge size
    val pocketTextSize = 13.dp.toPx()
    paint.typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
    paint.textSize = pocketTextSize

    val pocketTextWidth = paint.measureText(pocketText)
    val fontMetrics = paint.fontMetrics

    // Badge Dimensions & Padding
    val horizontalPadding = 6.dp.toPx()
    val verticalPadding = 2.dp.toPx()
    val badgeLeft = startX + gbWidth
    val badgeTop = baselineY + fontMetrics.ascent -  2 * verticalPadding
    val badgeRight = badgeLeft + pocketTextWidth + (horizontalPadding * 3)
    val badgeBottom = baselineY + fontMetrics.descent
    val cornerRadius = 3.dp.toPx()

    // 3. Draw the Badge Box (using fontColor / labelColor)
    val badgePaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = fontColor.toArgb()
        style = android.graphics.Paint.Style.FILL
    }

    canvas.drawRoundRect(
        badgeLeft,
        badgeTop,
        badgeRight,
        badgeBottom,
        cornerRadius,
        cornerRadius,
        badgePaint
    )

    // 4. Draw "pocket" text inside the badge (using lensColor)
    paint.color = lensColor.toArgb()
    canvas.drawText(
        pocketText,
        badgeLeft + horizontalPadding,
        baselineY - verticalPadding,
        paint
    )
}

private fun DrawScope.drawLightBezelBranding(
    canvas: android.graphics.Canvas,
    bezelRect: Rect,
    gameScreenRect: Rect?,
    fontColor: Color,
    lensColor: Color,
) {
    val gbText = "GAME BOY "
    val lightText = "LIGHT"

    val startX = gameScreenRect?.left ?: (bezelRect.left + 16.dp.toPx())
    val baselineY = (gameScreenRect?.bottom ?: (bezelRect.bottom - 40.dp.toPx())) + 24.dp.toPx()

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD_ITALIC)
        textSize = 22.dp.toPx()
    }

    paint.color = fontColor.toArgb()
    val gbWidth = paint.measureText(gbText)
    canvas.drawText(gbText, startX, baselineY, paint)

    paint.color = "#1CBCBA".toColorInt()
    canvas.drawText(lightText, startX + gbWidth, baselineY, paint)
}

private fun DrawScope.drawPocketNintendoBranding(
    canvas: android.graphics.Canvas,
    bezelRect: Rect,
) {
    val nintendoText = "Nintendo"
    val centerX = bezelRect.center.x
    val startY = bezelRect.bottom + 50.dp.toPx() // Positioned below bezel

    val brandingColor = Color.Black.copy(alpha = 0.1f).toArgb()

    // 1. Text Setup (Bigger font size)
    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = brandingColor
        typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
        textSize = 20.dp.toPx() // Increased from 12.dp
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val textWidth = textPaint.measureText(nintendoText)
    val fontMetrics = textPaint.fontMetrics

    // Draw "Nintendo" text
    canvas.drawText(nintendoText, centerX, startY, textPaint)

    // 2. Rounded Box Outline (Classic Pill Shape)
    val horizontalPadding = 12.dp.toPx()
    val verticalPadding = 4.dp.toPx()

    val strokePaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = brandingColor
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 2.5.dp.toPx()
    }

    val boxLeft = centerX - (textWidth / 2f) - horizontalPadding
    val boxTop = startY + fontMetrics.ascent - verticalPadding
    val boxRight = centerX + (textWidth / 2f) + horizontalPadding
    val boxBottom = startY + fontMetrics.descent + verticalPadding

    val boxHeight = boxBottom - boxTop
    val cornerRadius = boxHeight / 2f // Creates fully rounded pill ends

    canvas.drawRoundRect(
        boxLeft,
        boxTop,
        boxRight,
        boxBottom,
        cornerRadius,
        cornerRadius,
        strokePaint
    )
}

private fun DrawScope.drawSpeakerGrill(w: Float, h: Float) {
    val detailColor = Color.Black.copy(alpha = 0.2f)

    val grillCenterX = w - 80.dp.toPx()
    val grillCenterY = h - 150.dp.toPx()

    val totalLines = 6
    val lineSpacing = 20.dp.toPx()
    val lineHeight = 70.dp.toPx()
    val strokeWidthPx = 10.dp.toPx()

    val totalGrillWidth = (totalLines - 1) * lineSpacing

    withTransform({
        rotate(degrees = -30f, pivot = Offset(grillCenterX, grillCenterY))
    }) {
        val startX = grillCenterX - (totalGrillWidth / 2f)
        val startY = grillCenterY - (lineHeight / 2f)
        val endY = grillCenterY + (lineHeight / 2f)

        repeat(totalLines) { i ->
            val x = startX + (i * lineSpacing)
            drawLine(
                color = detailColor,
                start = Offset(x, startY),
                end = Offset(x, endY),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawPocketSpeakerGrill(w: Float, h: Float) {
    val detailColor = Color.Black.copy(alpha = 0.2f)

    val grillCenterX = w - 70.dp.toPx()
    val grillCenterY = h - 130.dp.toPx()

    val dotSpacing = 12.dp.toPx()
    val dotRadius = 3.5.dp.toPx()

    // Matrix specification per column: (dotCount, topRowOffset)
    // topRowOffset 0 starts at the middle row baseline.
    // topRowOffset -1 starts 1 step higher at the top.
    val columns = listOf(
        5 to 1,   // Col 1: 5 dots (rows 1 to 5)
        6 to 0,  // Col 2: 6 dots (rows 0 to 5, 1 higher at top)
        7 to -1,  // Col 3: 7 dots (rows 0 to 6)
        7 to -1,  // Col 4: 7 dots (rows 0 to 6)
        7 to -1,  // Col 5: 7 dots (rows 0 to 6)
        6 to -1,   // Col 6: 6 dots (rows 1 to 6, dropped 1 at top)
        5 to -1    // Col 7: 5 dots (rows 1 to 5)
    )

    val totalColumns = columns.size
    val totalWidth = (totalColumns - 1) * dotSpacing
    val startX = grillCenterX - (totalWidth / 2f)

    // Base Y corresponding to row 1 (the top row of a 5-dot column)
    val baseTopY = grillCenterY - (2 * dotSpacing)

    columns.forEachIndexed { colIndex, (dotCount, topRowOffset) ->
        val x = startX + (colIndex * dotSpacing)
        val startY = baseTopY + (topRowOffset * dotSpacing)

        repeat(dotCount) { rowIndex ->
            val y = startY + (rowIndex * dotSpacing)
            drawCircle(
                color = detailColor,
                radius = dotRadius,
                center = Offset(x, y)
            )
        }
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

