package com.swordfish.lemuroid.app.mobile.feature.home.cartridge

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidGameImage
import com.swordfish.lemuroid.lib.library.db.entity.Game

@Composable
fun GameBoyCartridge(
    game: Game,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        GameBoyCartridgeShell(
            modifier = Modifier.fillMaxSize()
        )

        // Game Box Art Label
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 68.dp, bottom = 32.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            LemuroidGameImage(
                game = game,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
                applyAspectRatio = false
            )
        }
    }
}

@Composable
private fun GameBoyCartridgeShell(
    modifier: Modifier = Modifier
) {
    val cartridgeColor = Color(0xFF3A3A3A) // Dark grey
    val detailColor = Color(0xFF4A4A4A)    // Shade lighter
    val lineInsetColor = Color(0xFF2A2A2A) // Shade darker

    val density = LocalDensity.current

    // Remember Native Paint object so it isn't created on every frame draw
    val textPaint = remember(cartridgeColor, density) {
        Paint().apply {
            color = cartridgeColor.toArgb()
            textSize = with(density) { 18.sp.toPx() }
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC)
            textAlign = Paint.Align.CENTER
        }
    }

    val shellPath = remember { Path() }
    val triPath = remember { Path() }

    Canvas(
        modifier = modifier.graphicsLayer() // Hardware-accelerated Skia layer caching
    ) {
        val w = size.width
        val h = size.height

        // 1. Body Path with top-right notch
        drawShellBody(
            path = shellPath,
            width = w,
            height = h,
            color = cartridgeColor
        )

        // 2. Side Grooves
        val grooveW = 4.dp.toPx()
        val grooveH = h * 0.75f
        val grooveY = h - grooveH

        drawRect(lineInsetColor, Offset(0f, grooveY), Size(grooveW, grooveH))
        drawRect(lineInsetColor, Offset(w - 4.dp.toPx(), grooveY), Size(grooveW, grooveH))

        // 3. Top Area Details & Lines
        val firstLineY = 26.dp.toPx()
        val lineSpacing = 6.dp.toPx()
        val lastLineY = firstLineY + 4 * lineSpacing

        repeat(5) { i ->
            val lineY = firstLineY + i * lineSpacing
            drawRect(
                color = lineInsetColor,
                topLeft = Offset(0f, lineY),
                size = Size(w, 3.dp.toPx())
            )
        }

        // Grip Oval Header
        val labelPadding = 16.dp.toPx()
        val ovalW = w - labelPadding - (2 * 16.dp.toPx())
        val ovalH = (lastLineY - firstLineY) + 16.dp.toPx()

        drawRoundRect(
            color = detailColor,
            topLeft = Offset(labelPadding + 8.dp.toPx(), firstLineY - 8.dp.toPx()),
            size = Size(ovalW, ovalH),
            cornerRadius = CornerRadius(ovalH / 2f)
        )

        // Logo Text
        drawContext.canvas.nativeCanvas.drawText(
            "GAME GRL",
            w / 2f + 32.dp.toPx(),
            firstLineY + ovalH / 2f,
            textPaint
        )

        // 4. Down Triangle Indicator
        drawDownTriangle(
            path = triPath,
            width = w,
            height = h,
            color = lineInsetColor
        )
    }
}

private fun DrawScope.drawShellBody(
    path: Path,
    width: Float,
    height: Float,
    color: Color
) {
    val r = 1.5.dp.toPx()
    val notchW = 16.dp.toPx()
    val notchH = 12.dp.toPx()

    path.reset()
    path.moveTo(r, 0f)
    path.lineTo(width - notchW, 0f)
    path.lineTo(width - notchW, notchH)
    path.lineTo(width, notchH)
    path.lineTo(width, height - r)
    path.quadraticTo(width, height, width - r, height)
    path.lineTo(r, height)
    path.quadraticTo(0f, height, 0f, height - r)
    path.lineTo(0f, r)
    path.quadraticTo(0f, 0f, r, 0f)
    path.close()

    drawPath(path, color)
}

private fun DrawScope.drawDownTriangle(
    path: Path,
    width: Float,
    height: Float,
    color: Color
) {
    val triW = 32.dp.toPx()
    val triH = 20.dp.toPx()
    val triBottomOffset = 4.dp.toPx()

    path.reset()
    path.moveTo(width / 2f - triW / 2f, height - triH - triBottomOffset)
    path.lineTo(width / 2f + triW / 2f, height - triH - triBottomOffset)
    path.lineTo(width / 2f, height - triBottomOffset)
    path.close()

    drawPath(path, color)
}
