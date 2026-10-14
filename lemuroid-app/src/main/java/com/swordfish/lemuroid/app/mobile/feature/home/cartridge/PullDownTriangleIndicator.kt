package com.swordfish.lemuroid.app.mobile.feature.home.cartridge

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun PullDownTriangleIndicator(
    modifier: Modifier = Modifier,
    isVisible: Boolean = true,
    count: Int = 3,
    color: Color = MaterialTheme.colorScheme.onSurface,
    baseTriangleWidth: Dp = 22.dp,
    baseTriangleHeight: Dp = 12.dp,
    spacing: Dp = 10.dp,
    animationDurationMillis: Int = 1800
) {
    // 1. Pre-calculate layout measurements and scales to avoid recomposition overhead
    val (scales, calculatedTotalHeightDp, canvasWidthDp) = remember(count, baseTriangleWidth, baseTriangleHeight, spacing) {
        val scaleStep = 0.35f
        val scaleList = List(count) { i -> 1f + ((count - 1 - i) * scaleStep) }

        var totalHeight = 0.dp
        scaleList.forEachIndexed { i, scale ->
            totalHeight += baseTriangleHeight * scale
            if (i < count - 1) totalHeight += spacing
        }
        totalHeight += 4.dp

        val maxWidth = baseTriangleWidth * (scaleList.firstOrNull() ?: 1f) + 4.dp
        Triple(scaleList, totalHeight, maxWidth)
    }

    // 2. Use rememberInfiniteTransition instead of a manual LaunchedEffect loop
    val infiniteTransition = rememberInfiniteTransition(label = "PullDownIndicatorTransition")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDurationMillis, easing = EaseInOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "PullDownIndicatorProgress"
    )

    // 3. Reuse a single Path instance across draw calls
    val reusablePath = remember { Path() }

    Canvas(
        modifier = modifier
            .width(canvasWidthDp)
            .height(calculatedTotalHeightDp)
    ) {
        val baseWidthPx = baseTriangleWidth.toPx()
        val baseHeightPx = baseTriangleHeight.toPx()
        val spacingPx = spacing.toPx()
        val xCenter = size.width / 2f
        val baseCornerRadiusPx = 3.dp.toPx()

        var currentTopY = 0f

        repeat(count) { index ->
            val scale = scales[index]
            val wPx = baseWidthPx * scale
            val hPx = baseHeightPx * scale
            val rPx = baseCornerRadiusPx * scale

            val phase = (progress - index * 0.22f + 1f) % 1f
            val intensity = if (phase < 0.5f) {
                val wave = sin(phase * 2f * PI.toFloat()).coerceAtLeast(0f)
                wave * wave
            } else {
                0f
            }

            val alpha = 0.25f + (0.7f * intensity)
            val translateY = currentTopY + (intensity * 2.dp.toPx())

            updateRoundedTrianglePath(
                path = reusablePath,
                xCenter = xCenter,
                topY = translateY,
                width = wPx,
                height = hPx,
                cornerRadius = rPx
            )

            drawPath(
                path = reusablePath,
                color = color.copy(alpha = alpha.coerceIn(0f, 1f))
            )

            currentTopY += hPx + spacingPx
        }
    }
}

// Reuse and update existing Path instead of allocating a new instance
private fun updateRoundedTrianglePath(
    path: Path,
    xCenter: Float,
    topY: Float,
    width: Float,
    height: Float,
    cornerRadius: Float
) {
    path.reset()

    val v1 = Offset(xCenter - width / 2f, topY)
    val v2 = Offset(xCenter + width / 2f, topY)
    val v3 = Offset(xCenter, topY + height)

    val halfW = width / 2f
    val edgeLen = sqrt(halfW * halfW + height * height)
    val d = cornerRadius.coerceAtMost(halfW.coerceAtMost(edgeLen / 2f))

    val dirRightX = -halfW / edgeLen
    val dirRightY = height / edgeLen

    val dirLeftX = -halfW / edgeLen
    val dirLeftY = -height / edgeLen

    val p1In = Offset(v1.x - d * dirLeftX, v1.y - d * dirLeftY)
    val p1Out = Offset(v1.x + d, v1.y)

    val p2In = Offset(v2.x - d, v2.y)
    val p2Out = Offset(v2.x + d * dirRightX, v2.y + d * dirRightY)

    val p3In = Offset(v3.x - d * dirRightX, v3.y - d * dirRightY)
    val p3Out = Offset(v3.x + d * dirLeftX, v3.y + d * dirLeftY)

    path.moveTo(p1Out.x, p1Out.y)
    path.lineTo(p2In.x, p2In.y)
    path.quadraticTo(v2.x, v2.y, p2Out.x, p2Out.y)
    path.lineTo(p3In.x, p3In.y)
    path.quadraticTo(v3.x, v3.y, p3Out.x, p3Out.y)
    path.lineTo(p1In.x, p1In.y)
    path.quadraticTo(v1.x, v1.y, p1Out.x, p1Out.y)
    path.close()
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PullDownTriangleIndicatorPreview() {
    MaterialTheme {
        PullDownTriangleIndicator(
            isVisible = true,
            color = Color.White
        )
    }
}
