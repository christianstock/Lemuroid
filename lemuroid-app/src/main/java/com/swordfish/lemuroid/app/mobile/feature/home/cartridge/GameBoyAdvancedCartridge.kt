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
fun GameBoyAdvancedCartridge(
    game: Game,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        GameBoyAdvancedCartridgeShell(
            modifier = Modifier.fillMaxSize()
        )

        // Box Art Label (Positioned under top overhang and inside side indents)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 44.dp, bottom = 32.dp)
                .clip(RoundedCornerShape(8.dp)),
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
private fun GameBoyAdvancedCartridgeShell(
    modifier: Modifier = Modifier
) {
    val cartridgeColor = Color(0xFF3A3A3A) // Dark grey
    val detailColor = Color(0xFF4A4A4A)    // Shade lighter
    val lineInsetColor = Color(0xFF2A2A2A) // Shade darker

    val density = LocalDensity.current

    // Remember Native Paint objects to prevent allocations during animation frames
    val gameBoyPaint = remember(cartridgeColor, density) {
        Paint().apply {
            color = cartridgeColor.toArgb()
            textSize = with(density) { 14.sp.toPx() }
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC)
            textAlign = Paint.Align.CENTER
        }
    }

    val advancePaint = remember(cartridgeColor, density) {
        Paint().apply {
            color = cartridgeColor.toArgb()
            textSize = with(density) { 12.sp.toPx() }
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC)
            textAlign = Paint.Align.CENTER
        }
    }

    val shellPath = remember { Path() }
    val triPath = remember { Path() }

    Canvas(
        modifier = modifier.graphicsLayer() // Caches vector draw calls in Skia hardware layer
    ) {
        val w = size.width
        val h = size.height
        val overhangH = 36.dp.toPx()
        val sideIndent = 6.dp.toPx()

        // 1. T-Shaped GBA Body
        drawGbaShellBody(
            path = shellPath,
            width = w,
            height = h,
            overhangH = overhangH,
            sideIndent = sideIndent,
            color = cartridgeColor
        )

        // 2. Top Half-Oval Inset
        val ovalW = w * 0.8f
        val ovalH = 56.dp.toPx()
        drawArc(
            color = detailColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset((w - ovalW) / 2f, 4.dp.toPx()),
            size = Size(ovalW, ovalH)
        )

        // Logo Text ("GAME BOY ADVANCE")
        val firstLineY = 4.dp.toPx()
        val textCenterY = firstLineY + ovalH / 2f

        drawContext.canvas.nativeCanvas.drawText(
            "GAME GRL",
            w / 2f - 32.dp.toPx(),
            textCenterY - 4.dp.toPx(),
            gameBoyPaint
        )

        drawContext.canvas.nativeCanvas.drawText(
            "ADVANCE",
            w / 2f + 32.dp.toPx(),
            textCenterY - 6.dp.toPx(),
            advancePaint
        )

        // 3. Bottom Down Triangle Indicator
        drawDownTriangle(
            path = triPath,
            width = w,
            height = h,
            color = lineInsetColor
        )
    }
}

private fun DrawScope.drawGbaShellBody(
    path: Path,
    width: Float,
    height: Float,
    overhangH: Float,
    sideIndent: Float,
    color: Color
) {
    val cornerRadius = 4.dp.toPx()

    path.reset()
    // Top overhang
    path.moveTo(cornerRadius, 0f)
    path.lineTo(width - cornerRadius, 0f)
    path.quadraticTo(width, 0f, width, cornerRadius)
    path.lineTo(width, overhangH)

    // Indent to main narrow body
    path.lineTo(width - sideIndent, overhangH)
    path.lineTo(width - sideIndent, height - cornerRadius)
    path.quadraticTo(width - sideIndent, height, width - sideIndent - cornerRadius, height)
    path.lineTo(sideIndent + cornerRadius, height)
    path.quadraticTo(sideIndent, height, sideIndent, height - cornerRadius)
    path.lineTo(sideIndent, overhangH)

    // Back out to left overhang
    path.lineTo(0f, overhangH)
    path.lineTo(0f, cornerRadius)
    path.quadraticTo(0f, 0f, cornerRadius, 0f)
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
    val triBottomOffset = 6.dp.toPx()

    path.reset()
    path.moveTo(width / 2f - triW / 2f, height - triH - triBottomOffset)
    path.lineTo(width / 2f + triW / 2f, height - triH - triBottomOffset)
    path.lineTo(width / 2f, height - triBottomOffset)
    path.close()

    drawPath(path, color)
}
