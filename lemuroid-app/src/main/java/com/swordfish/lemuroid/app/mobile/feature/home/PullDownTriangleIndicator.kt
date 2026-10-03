package com.swordfish.lemuroid.app.mobile.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
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
    val scaleStep = 0.35f
    val scales = List(count) { i -> 1f + ((count - 1 - i) * scaleStep) }

    var calculatedTotalHeightDp = 0.dp
    val itemHeightsDp = scales.map { baseTriangleHeight * it }
    itemHeightsDp.forEachIndexed { i, h ->
        calculatedTotalHeightDp += h
        if (i < count - 1) calculatedTotalHeightDp += spacing
    }
    calculatedTotalHeightDp += 4.dp

    val maxTriangleWidthDp = baseTriangleWidth * (scales.firstOrNull() ?: 1f)
    val canvasWidthDp = maxTriangleWidthDp + 4.dp

    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            progressAnim.snapTo(0f)
            while (isActive) {
                progressAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = animationDurationMillis,
                        easing = LinearEasing
                    )
                )
                progressAnim.snapTo(0f)
            }
        } else {
            progressAnim.snapTo(0f)
        }
    }

    val progress = progressAnim.value

    Canvas(
        modifier = modifier
            .width(canvasWidthDp)
            .height(calculatedTotalHeightDp)
    ) {
        val baseWidthPx = baseTriangleWidth.toPx()
        val baseHeightPx = baseTriangleHeight.toPx()
        val spacingPx = spacing.toPx()
        val xCenter = canvasWidthDp.toPx() / 2f
        val baseCornerRadiusPx = 3.dp.toPx()

        var currentTopY = 0f

        repeat(count) { index ->
            val scale = scales[index]
            val wPx = baseWidthPx * scale
            val hPx = baseHeightPx * scale
            val rPx = baseCornerRadiusPx * scale

            val phase = (progress - (index * 0.22f) + 1f) % 1f
            val intensity = if (phase < 0.5f) {
                val wave = sin(phase * 2f * PI.toFloat()).coerceAtLeast(0f)
                wave * wave
            } else {
                0f
            }

            val alpha = 0.25f + (0.7f * intensity)
            val translateY = currentTopY + (intensity * 2.dp.toPx())

            val trianglePath = createRoundedTrianglePath(
                xCenter = xCenter,
                topY = translateY,
                width = wPx,
                height = hPx,
                cornerRadius = rPx
            )

            drawPath(
                path = trianglePath,
                color = color.copy(alpha = alpha.coerceIn(0f, 1f))
            )

            currentTopY += hPx + spacingPx
        }
    }
}

private fun createRoundedTrianglePath(
    xCenter: Float,
    topY: Float,
    width: Float,
    height: Float,
    cornerRadius: Float
): Path {
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

    return Path().apply {
        moveTo(p1Out.x, p1Out.y)
        lineTo(p2In.x, p2In.y)
        quadraticTo(v2.x, v2.y, p2Out.x, p2Out.y)
        lineTo(p3In.x, p3In.y)
        quadraticTo(v3.x, v3.y, p3Out.x, p3Out.y)
        lineTo(p1In.x, p1In.y)
        quadraticTo(v1.x, v1.y, p1Out.x, p1Out.y)
        close()
    }
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
