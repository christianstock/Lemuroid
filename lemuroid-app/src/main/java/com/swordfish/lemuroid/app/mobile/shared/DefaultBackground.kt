package com.swordfish.lemuroid.app.mobile.shared

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Atmospheric ambient glow background used when no box art cover is available.
 */
@Composable
fun DefaultBackground(
    glowColor: Color = RetroLcdGreen,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DefaultGlowTransition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulseRadius"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .radialVignette(
                centerColor = Color.Transparent,
                radiusMultiplier = 0.85f
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 80.dp)
        ) {
            val centerOffset = Offset(size.width / 2f, size.height * 0.45f)
            val maxRadius = size.maxDimension * pulseScale

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = pulseAlpha),
                        glowColor.copy(alpha = pulseAlpha * 0.4f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = maxRadius
                ),
                center = centerOffset,
                radius = maxRadius
            )
        }
    }
}
