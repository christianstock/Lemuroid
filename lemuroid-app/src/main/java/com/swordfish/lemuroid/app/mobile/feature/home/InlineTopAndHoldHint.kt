package com.swordfish.lemuroid.app.mobile.feature.home

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * Minimalist inline TAP / INFO hint with opacity-only pulsing (size remains fixed).
 */
@Composable
fun InlineTapHint(
    label: String,
    modifier: Modifier = Modifier
) {
    val pulseTransition = rememberInfiniteTransition(label = "TapOpacityPulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.graphicsLayer { alpha = pulseAlpha }
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val centerPx = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = Color.White,
                radius = size.width / 2f,
                style = Stroke(width = 1.25.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                radius = size.width / 3.5f,
                style = Stroke(width = 1.25.dp.toPx())
            )
        }
        Text(
            text = label,
            fontFamily = PressStart2PFontFamily,
            fontSize = 11.sp,
            color = Color.White
        )
    }
}

/**
 * Minimalist inline HOLD / LIST hint with opacity-only pulsing (size remains fixed).
 */
@Composable
fun InlineHoldHint(
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val durationMs = 5000 // Matches the overall hint visibility window

    val transition = rememberInfiniteTransition(label = "SingleHoldWindupTransition")

    // Synchronized opacity: Fades in, holds, then fades out smoothly over the single duration
    val pulseTransition = rememberInfiniteTransition(label = "TapOpacityPulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // Exactly ONE 360° rotation over the full time span
    val sweepAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMs,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "Single360Windup"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.graphicsLayer { alpha = pulseAlpha }
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val centerPx = Offset(size.width / 2f, size.height / 2f)
            val innerRadius = size.width / 3.5f
            val outerRadius = size.width / 2f

            // Inner guide circle
            drawCircle(
                color = Color.White,
                radius = innerRadius,
                center = centerPx,
                style = Stroke(width = 1.25.dp.toPx())
            )

            // Outer arc executing one full 360° windup
            if (sweepAngle > 0f) {
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerPx.x - outerRadius, centerPx.y - outerRadius),
                    size = Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        Text(
            text = label,
            fontFamily = PressStart2PFontFamily,
            fontSize = 11.sp,
            color = Color.White
        )
    }
}
