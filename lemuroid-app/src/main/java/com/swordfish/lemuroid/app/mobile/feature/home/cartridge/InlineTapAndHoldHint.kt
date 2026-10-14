package com.swordfish.lemuroid.app.mobile.feature.home.cartridge

import androidx.compose.animation.core.EaseInOutCubic
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
import androidx.compose.runtime.remember
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
import com.swordfish.lemuroid.app.mobile.feature.home.PressStart2PFontFamily

/**
 * Shared layout wrapper for inline gesture hints.
 */
@Composable
private fun InlineHintLayout(
    label: String,
    pulseAlpha: Float,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.graphicsLayer { alpha = pulseAlpha }
    ) {
        icon()
        Text(
            text = label,
            fontFamily = PressStart2PFontFamily,
            fontSize = 11.sp,
            color = Color.White
        )
    }
}

/**
 * Shared opacity pulsing transition hook.
 */
@Composable
private fun rememberPulseAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "InlineHintPulseTransition")
    val alpha by transition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2500, // Increased duration for a single longer swell
                easing = EaseInOutCubic // Smooth, gradual ramp up and down
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )
    return alpha
}

/**
 * Minimalist inline TAP / INFO hint with opacity-only pulsing.
 */
@Composable
fun InlineTapHint(
    label: String,
    modifier: Modifier = Modifier,
    pulseAlpha: Float = rememberPulseAlpha()
) {
    InlineHintLayout(
        label = label,
        pulseAlpha = pulseAlpha,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val strokeWidthPx = 1.25.dp.toPx()
            val outerRadius = size.width / 2f
            val innerRadius = size.width / 3.5f

            drawCircle(
                color = Color.White,
                radius = outerRadius,
                style = Stroke(width = strokeWidthPx)
            )
            drawCircle(
                color = Color.White,
                radius = innerRadius,
                style = Stroke(width = strokeWidthPx)
            )
        }
    }
}

/**
 * Minimalist inline HOLD / LIST hint with synchronized windup ring animation.
 */
@Composable
fun InlineHoldHint(
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    pulseAlpha: Float = rememberPulseAlpha()
) {
    val transition = rememberInfiniteTransition(label = "SingleHoldWindupTransition")
    val sweepAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Single360Windup"
    )

    InlineHintLayout(
        label = label,
        pulseAlpha = pulseAlpha,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val centerPx = Offset(size.width / 2f, size.height / 2f)
            val innerRadius = size.width / 3.5f
            val outerRadius = size.width / 2f
            val strokeWidthPx = 1.25.dp.toPx()
            val arcStrokeWidthPx = 2.dp.toPx()

            // Inner guide circle
            drawCircle(
                color = Color.White,
                radius = innerRadius,
                center = centerPx,
                style = Stroke(width = strokeWidthPx)
            )

            // Outer arc executing one full 360° windup
            if (sweepAngle > 0f) {
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(centerPx.x - outerRadius, centerPx.y - outerRadius),
                    size = Size(outerRadius * 2f, outerRadius * 2f),
                    style = Stroke(width = arcStrokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }
    }
}
