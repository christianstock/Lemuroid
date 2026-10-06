package com.swordfish.lemuroid.app.mobile.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun HoldGestureOverlay(
    pressOffset: Offset,
    holdProgress: Float,
    tintColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val centerPx = if (pressOffset != Offset.Unspecified) {
            pressOffset
        } else {
            Offset(size.width / 2f, size.height / 2f)
        }

        val innerRadius = 24.dp.toPx()
        val outerRadius = 40.dp.toPx()

        // Inner thin circle
        drawCircle(
            color = Color.White.copy(alpha = 0.65f),
            radius = innerRadius,
            center = centerPx,
            style = Stroke(width = 1.25.dp.toPx())
        )

        // Outer guide circle
        drawCircle(
            color = Color.White.copy(alpha = 0.25f),
            radius = outerRadius,
            center = centerPx,
            style = Stroke(width = 1.25.dp.toPx())
        )

        // Animated progress arc
        if (holdProgress > 0f) {
            drawArc(
                color = tintColor,
                startAngle = -90f,
                sweepAngle = 360f * holdProgress,
                useCenter = false,
                topLeft = Offset(centerPx.x - outerRadius, centerPx.y - outerRadius),
                size = Size(outerRadius * 2, outerRadius * 2),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
