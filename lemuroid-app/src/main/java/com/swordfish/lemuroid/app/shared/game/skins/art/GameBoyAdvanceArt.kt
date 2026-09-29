package com.swordfish.lemuroid.app.shared.game.skins.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyAdvanceSkin
import com.swordfish.lemuroid.app.shared.game.skins.GameBoyModel
import com.swordfish.lemuroid.app.shared.game.skins.GameBoySkin

object GameBoyAdvanceArt {
    fun DrawScope.drawHandheld(
        gameScreenRect: Rect?,
        bezelRect: Rect?,
        skin: GameBoyAdvanceSkin,
        isCarouselMode: Boolean,
        batteryLevel: Float,
    ) {
        val width = size.width
        val height = size.height
        val corner = 24.dp.toPx()

        // 1. Draw Shell
        if (isCarouselMode) {
            val path = Path().apply {
                addRoundRect(
                    RoundRect(
                        Rect(0f, 0f, width, height),
                        topLeft = CornerRadius(corner),
                        topRight = CornerRadius(corner),
                        bottomLeft = CornerRadius.Zero,
                        bottomRight = CornerRadius.Zero
                    )
                )
            }
            drawPath(path, skin.caseColor)
        } else {
            if (bezelRect != null) {
                val bezelPath = calculateBezelPath(bezelRect)
                val shellPath = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(Rect(0f, 0f, width, height))
                    addPath(bezelPath)
                }
                drawPath(shellPath, skin.caseColor)
            } else {
                drawRect(skin.caseColor)
            }
        }

        // 2. Bezel / Details
        bezelRect?.let { rect ->
            val brandingColor = Color.Black.copy(alpha = 0.4f)

            if (isCarouselMode) {
                val bezelPath = calculateBezelPath(rect)
                drawPath(bezelPath, Color(0xFF1A1A1A))

                val shoulderW = width * 0.2f
                val shoulderH = 20.dp.toPx()
                drawRect(
                    color = Color.Black.copy(alpha = 0.1f),
                    topLeft = Offset(0f, 0f),
                    size = Size(shoulderW, shoulderH)
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.1f),
                    topLeft = Offset(width - shoulderW, 0f),
                    size = Size(shoulderW, shoulderH)
                )
            }
        }

        if (bezelRect != null) {
            drawPowerIndicator(
                canvas = drawContext.canvas.nativeCanvas,
                bezelRect = bezelRect,
                gameScreenRect = gameScreenRect,
                skin = skin,
                batteryLevel = batteryLevel,
            )
        }

        if (!isCarouselMode) {
            drawSpeakerGrill(width, height)
        }
    }

    private fun DrawScope.calculateBezelPath(rect: Rect): Path {
        val topCornerPx = 24.dp.toPx()
        val bottomCornerPx = 36.dp.toPx()           // Radii come out further at the bottom
        val bottomCornerOutsetPx = 12.dp.toPx()      // Flare bottom corners outward horizontally
        val topBulgePx = 10.dp.toPx()                // Slight top bulge upward
        val bottomBulgePx = 16.dp.toPx()             // Bottom bulge downward

        val bRightX = rect.right + bottomCornerOutsetPx
        val bLeftX = rect.left - bottomCornerOutsetPx

        return Path().apply {
            // Start top-left after corner
            moveTo(rect.left + topCornerPx, rect.top)

            // Top edge: Slight top bulge curved up towards center
            quadraticTo(
                rect.center.x, rect.top - topBulgePx,
                rect.right - topCornerPx, rect.top
            )

            // Top-Right corner
            quadraticTo(rect.right, rect.top, rect.right, rect.top + topCornerPx)

            // Right side down to bottom-right corner outset
            lineTo(bRightX, rect.bottom - bottomCornerPx)

            // Bottom-Right flared corner
            quadraticTo(bRightX, rect.bottom, bRightX - bottomCornerPx, rect.bottom)

            // Bottom edge: Main bottom bulge curved down towards center
            quadraticTo(
                rect.center.x, rect.bottom + bottomBulgePx,
                bLeftX + bottomCornerPx, rect.bottom
            )

            // Bottom-Left flared corner
            quadraticTo(bLeftX, rect.bottom, bLeftX, rect.bottom - bottomCornerPx)

            lineTo(rect.left, rect.top + topCornerPx)

            quadraticTo(rect.left, rect.top, rect.left + topCornerPx, rect.top)

            close()
        }
    }
}

private fun DrawScope.drawSpeakerGrill(w: Float, h: Float) {
    val detailColor = Color.Black.copy(alpha = 0.2f)

    val grillCenterX = w - 120.dp.toPx()
    val grillCenterY = h - 100.dp.toPx()

    val totalLines = 5
    val lineSpacing = 16.dp.toPx()
    val lineWidth = 100.dp.toPx()
    val strokeWidthPx = 6.dp.toPx()

    val dy = -6.dp.toPx()
    val halfWidth = lineWidth / 2f
    val totalGrillHeight = (totalLines - 1) * lineSpacing

    val startY = grillCenterY - (totalGrillHeight / 2f)

    repeat(totalLines) { i ->
        val y = startY + (i * lineSpacing)

        drawLine(
            color = detailColor,
            start = Offset(grillCenterX - halfWidth, y - dy),
            end = Offset(grillCenterX + halfWidth, y + dy),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawPowerIndicator(
    canvas: android.graphics.Canvas,
    bezelRect: Rect,
    gameScreenRect: Rect?,
    skin: GameBoyAdvanceSkin,
    batteryLevel: Float
) {
    val screenRight = gameScreenRect?.right ?: (bezelRect.right + 24.dp.toPx())
    val screenCenterY = gameScreenRect?.center?.y ?: (bezelRect.top + 60.dp.toPx())

    val textX = screenRight + 170.dp.toPx()
    val ledX = screenRight + 130.dp.toPx()
    val ledY = screenCenterY - 120.dp.toPx()
    val ledRadius = 8.dp.toPx()
    val textY = ledY + 5.dp.toPx()


    val alphaInt = (batteryLevel.coerceIn(0.2f, 1.0f) * 255).toInt()
    val ledColor = android.graphics.Color.argb(alphaInt, 20, 250, 20)

    val fillPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = ledColor
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawCircle(ledX, ledY, ledRadius, fillPaint)

    val labelColor = Color.Black.copy(alpha = 0.2f).toArgb()

    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = labelColor
        typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
        textSize = 14.sp.toPx()
        textAlign = android.graphics.Paint.Align.CENTER
    }

    canvas.drawText("POWER", textX, textY, textPaint)
}
