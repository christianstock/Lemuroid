package com.swordfish.lemuroid.app.shared.game.skins.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyColorModel
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyColorSkin
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyModel
import com.swordfish.lemuroid.app.shared.game.skins.GameBoySkin

object GameBoyColorArt {
    fun DrawScope.drawHandheld(
        gameScreenRect: Rect?,
        bezelRect: Rect?,
        skin: GameBoyColorSkin,
        isCarouselMode: Boolean,
        batteryLevel: Float,
    ) {
        val width = size.width
        val height = size.height
        val corner = 16.dp.toPx()

        drawConsoleBackground(skin.caseColor, width, height, isCarouselMode)

        bezelRect?.let { rect ->
            drawScreenLens(rect)

            drawPowerIndicator(
                canvas = drawContext.canvas.nativeCanvas,
                bezelRect = rect,
                gameScreenRect = gameScreenRect,
                skin = skin,
                batteryLevel = batteryLevel,
            )

            if (skin.model == GameBoyColorModel.CBG_01) {
                drawColorBezelBranding(
                    canvas = drawContext.canvas.nativeCanvas,
                    bezelRect = rect,
                    gameScreenRect = gameScreenRect,
                    fontColor = skin.brandingColor,
                )

                drawColorNintendoBranding(
                    canvas = drawContext.canvas.nativeCanvas,
                    bezelRect = rect
                )

                if (!isCarouselMode) {
                    drawColorSpeakerGrill(width, height)
                }
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
) {
    val bezelPath = calculateBezelPath(bezelRect)
    val strokeColor = Color.Black.copy(alpha = 0.2f)
    val borderWidth = 1.5.dp.toPx()

    drawPath(
        path = bezelPath,
        color = Color.Transparent,
        blendMode = BlendMode.Clear
    )

    drawPath(
        path = bezelPath,
        color = strokeColor,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth)
    )
}

private fun DrawScope.drawColorBezelBranding(
    canvas: android.graphics.Canvas,
    bezelRect: Rect,
    gameScreenRect: Rect?,
    fontColor: Color,
) {
    val gbText = "GAME BOY "
    val c1Text = "C"
    val o2Text = "O"
    val l3Text = "L"
    val o4Text = "O"
    val r5Text = "R"

    val startX = gameScreenRect?.left ?: (bezelRect.left + 16.dp.toPx())
    val baselineY = (gameScreenRect?.bottom ?: (bezelRect.bottom - 40.dp.toPx())) + 24.dp.toPx()

    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD_ITALIC)
        textSize = 22.dp.toPx()
    }

    paint.color = fontColor.toArgb()
    var gbWidth = paint.measureText(gbText)
    canvas.drawText(gbText, startX, baselineY, paint)

    paint.color = "#C81F55".toColorInt()
    paint.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD_ITALIC)
    canvas.drawText(c1Text, startX + gbWidth, baselineY, paint)
    gbWidth += paint.measureText(c1Text)

    paint.color = "#4D3380".toColorInt()
    canvas.drawText(o2Text, startX + gbWidth, baselineY, paint)
    gbWidth += paint.measureText(o2Text)

    paint.color = "#76C043".toColorInt()
    canvas.drawText(l3Text, startX + gbWidth, baselineY, paint)
    gbWidth += paint.measureText(l3Text)

    paint.color = "#F9C623".toColorInt()
    canvas.drawText(o4Text, startX + gbWidth, baselineY, paint)
    gbWidth += paint.measureText(o4Text)

    paint.color = "#008B9B".toColorInt()
    canvas.drawText(r5Text, startX + gbWidth, baselineY, paint)
}

private fun DrawScope.drawColorNintendoBranding(
    canvas: android.graphics.Canvas,
    bezelRect: Rect,
) {
    val nintendoText = "Nintendo"
    val centerX = bezelRect.center.x
    val startY = bezelRect.bottom + 50.dp.toPx()

    val brandingColor = Color.Black.copy(alpha = 0.1f).toArgb()

    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = brandingColor
        typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
        textSize = 20.dp.toPx()
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val textWidth = textPaint.measureText(nintendoText)
    val fontMetrics = textPaint.fontMetrics

    canvas.drawText(nintendoText, centerX, startY, textPaint)

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
    val cornerRadius = boxHeight / 2f

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

private fun DrawScope.drawColorSpeakerGrill(w: Float, h: Float) {
    val detailColor = Color.Black.copy(alpha = 0.2f)

    val grillCenterX = w - 70.dp.toPx()
    val grillCenterY = h - 130.dp.toPx()

    val dotSpacing = 12.dp.toPx()
    val dotRadius = 3.5.dp.toPx()

    val columns = listOf(
        5 to 1,
        6 to 0,
        7 to -1,
        7 to -1,
        7 to -1,
        6 to -1,
        5 to -1
    )

    val totalColumns = columns.size
    val totalWidth = (totalColumns - 1) * dotSpacing
    val startX = grillCenterX - (totalWidth / 2f)

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

private fun DrawScope.drawPowerIndicator(
    canvas: android.graphics.Canvas,
    bezelRect: Rect,
    gameScreenRect: Rect?,
    skin: GameBoyColorSkin,
    batteryLevel: Float
) {
    val screenLeft = gameScreenRect?.left ?: (bezelRect.left + 24.dp.toPx())
    val screenCenterY = gameScreenRect?.center?.y ?: (bezelRect.top + 60.dp.toPx())

    val textX = screenLeft - 22.dp.toPx()
    val ledX = screenLeft - 30.dp.toPx()
    val ledY = screenCenterY - 70.dp.toPx()
    val ledRadius = 5.dp.toPx()
    val textY = ledY + 20.dp.toPx()


    val alphaInt = (batteryLevel.coerceIn(0.2f, 1.0f) * 255).toInt()
    val ledColor = android.graphics.Color.argb(alphaInt, 250, 80, 20)

    val fillPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = ledColor
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(ledX, ledY, ledRadius, fillPaint)

    val moonPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = skin.brandingColor.copy(alpha = 1.0f).toArgb()
    }

    val moonWidth = 5.dp.toPx()
    val moonHeight = 7.dp.toPx()
    val crescentWidth = 3.dp.toPx()
    val moonSpacing = 8.dp.toPx()
    val startX = ledX + 8.dp.toPx()

    repeat(2) { i ->
        val cx = startX + (i * moonSpacing)

        val moonPath = android.graphics.Path().apply {
            val outerRect = android.graphics.RectF(
                cx - moonWidth,
                ledY - moonHeight,
                cx + moonWidth,
                ledY + moonHeight
            )
            arcTo(outerRect, -90f, 180f, false)

            val innerRect = android.graphics.RectF(
                cx - moonWidth + crescentWidth,
                ledY - moonHeight,
                cx + moonWidth - crescentWidth,
                ledY + moonHeight
            )
            arcTo(innerRect, 90f, -180f, false)

            close()
        }

        canvas.drawPath(moonPath, moonPaint)
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
