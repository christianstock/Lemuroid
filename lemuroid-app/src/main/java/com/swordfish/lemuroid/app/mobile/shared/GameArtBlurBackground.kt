package com.swordfish.lemuroid.app.mobile.shared

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun GameArtBlurBackground(
    coverFrontUrl: String?,
    accentColor: Color = RetroLcdGreen,
    animationSpec: FiniteAnimationSpec<Float> = tween(durationMillis = 600),
    modifier: Modifier = Modifier
) {
    Crossfade(
        targetState = coverFrontUrl,
        animationSpec = animationSpec,
        label = "BackgroundBlurCrossfade",
        modifier = modifier.fillMaxSize()
    ) { coverUrl ->
        if (!coverUrl.isNullOrEmpty()) {
            BlurredCoverBackground(coverUrl = coverUrl, ambientColor = accentColor)
        } else {
            DefaultBackground(glowColor = accentColor)
        }
    }
}

@Composable
private fun BlurredCoverBackground(
    coverUrl: String,
    ambientColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .radialVignette(
                centerColor = Color.Transparent,
                radiusMultiplier = 0.85f
            )
    ) {
        // Base blurred cover art
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(coverUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 160.dp)
                .graphicsLayer {
                    scaleX = 1.25f
                    scaleY = 1.25f
                    alpha = 0.85f
                }
        )
    }
}

/**
 * Vignette overlay that frames the screen in dark cinema contrast.
 */
fun Modifier.radialVignette(
    centerColor: Color = Color.Transparent,
    radiusMultiplier: Float = 0.85f,
    verticalBias: Float = 0.5f
): Modifier = this.drawWithContent {
    drawContent()

    // Slight uniform dimming wash
    drawRect(Color.Black.copy(alpha = 0.20f))

    // Edge-darkening radial fade
    val centerOffset = Offset(size.width / 2f, size.height * verticalBias)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                centerColor,
                Color.Black.copy(alpha = 0.45f),
                Color.Black.copy(alpha = 0.95f)
            ),
            center = centerOffset,
            radius = size.maxDimension * radiusMultiplier
        )
    )
}
